package de.rieckpil.library;

import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.rieckpil.library.configuration.ApplicationProperties;
import de.rieckpil.library.model.BookLoan;
import de.rieckpil.library.model.Notification;
import de.rieckpil.library.model.Notification.NotificationStatus;
import de.rieckpil.library.model.Notification.NotificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class NotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

  private final NotificationRepository notificationRepository;
  private final BookLoanRepository bookLoanRepository;
  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;
  private final ApplicationProperties applicationProperties;
  private final JobScheduler jobScheduler;

  public NotificationService(
    NotificationRepository notificationRepository,
    BookLoanRepository bookLoanRepository,
    JavaMailSender mailSender,
    TemplateEngine templateEngine,
    ApplicationProperties applicationProperties,
    JobScheduler jobScheduler) {
    this.notificationRepository = notificationRepository;
    this.bookLoanRepository = bookLoanRepository;
    this.mailSender = mailSender;
    this.templateEngine = templateEngine;
    this.applicationProperties = applicationProperties;
    this.jobScheduler = jobScheduler;
  }

  public List<Notification> filterNotifications(String status, String type, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));

    if (status != null && !status.isEmpty() && type != null && !type.isEmpty()) {
      return notificationRepository.findByStatusAndType(
        NotificationStatus.valueOf(status.toUpperCase()),
        NotificationType.valueOf(type.toUpperCase()),
        pageRequest);
    }
    else if (status != null && !status.isEmpty()) {
      return notificationRepository.findByStatus(
        NotificationStatus.valueOf(status.toUpperCase()), pageRequest);
    }
    else if (type != null && !type.isEmpty()) {
      return notificationRepository.findByType(
        NotificationType.valueOf(type.toUpperCase()), pageRequest);
    }
    else {
      return getAllNotifications(page, size);
    }
  }

  public List<Notification> getAllNotifications(int page, int size) {
    return notificationRepository
      .findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt")))
      .getContent();
  }

  @Transactional
  public Notification resendNotification(UUID id) {
    Notification notification =
      notificationRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + id));

    if (notification.getStatus() != NotificationStatus.FAILED) {
      throw new IllegalStateException(
        "Only failed notifications can be resent. Current status: " + notification.getStatus());
    }

    // Update notification for resending
    notification.setStatus(NotificationStatus.QUEUED);
    notification.setSentAt(ZonedDateTime.now());
    final Notification updatedNotification = notificationRepository.save(notification);

    // Enqueue the job to send the notification
    jobScheduler.enqueue(() -> sendNotificationById(updatedNotification.getId()));

    return notification;
  }

  @Transactional
  @Job(name = "Send notification by ID")
  public void sendNotificationById(UUID notificationId) {
    Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);

    if (notificationOpt.isEmpty()) {
      LOG.warn("Notification not found: {}", notificationId);
      return;
    }

    Notification notification = notificationOpt.get();
    if (notification.getStatus() != NotificationStatus.QUEUED) {
      LOG.info(
        "Notification {} is not in QUEUED status, current status: {}",
        notificationId,
        notification.getStatus());
      return;
    }

    try {
      if (notification.getType() == NotificationType.EMAIL) {
        sendEmailNotification(notification);
      }
      else {
        LOG.warn("Unsupported notification type: {}", notification.getType());
        notification.setStatus(NotificationStatus.FAILED);
        notificationRepository.save(notification);
      }
    }
    catch (Exception e) {
      LOG.error("Error sending notification {}: {}", notificationId, e.getMessage(), e);
      notification.setStatus(NotificationStatus.FAILED);
      notification.setErrorMessage(e.getMessage());
      notificationRepository.save(notification);
    }
  }

  private void sendEmailNotification(Notification notification) throws MessagingException, UnsupportedEncodingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setFrom(
      applicationProperties.getEmail().getFrom(),
      applicationProperties.getEmail().getName());
    helper.setTo(notification.getLoan().getUser().getEmail());
    helper.setSubject(notification.getSubject());
    helper.setText(notification.getContent(), true);

    // Could add logo as attachment here
    // FileSystemResource logo = new FileSystemResource("path/to/logo.png");
    // helper.addInline("logo", logo);

    // Send email
    mailSender.send(message);

    // Update notification status
    notification.setStatus(NotificationStatus.SENT);
    notificationRepository.save(notification);

    LOG.info(
      "Email notification sent successfully to {} for loan {}",
      notification.getLoan().getUser().getEmail(),
      notification.getLoan().getId());
  }

  @Transactional
  @Job(name = "Send due date reminders")
  public void sendDueDateReminders() {
    int reminderDays = applicationProperties.getLoan().getReminderDaysBeforeDue();
    ZonedDateTime reminderStart = ZonedDateTime.now();
    ZonedDateTime reminderEnd = reminderStart.plusDays(1);

    List<BookLoan> loansNeedingReminders =
      bookLoanRepository.findLoansNeedingReminders(reminderStart, reminderEnd);

    LOG.info("Found {} loans needing due date reminders", loansNeedingReminders.size());

    for (BookLoan loan : loansNeedingReminders) {
      try {
        createAndSendDueDateReminder(loan);
      }
      catch (Exception e) {
        LOG.error("Error sending due date reminder for loan {}: {}", loan.getId(), e.getMessage(), e);
      }
    }
  }

  @Transactional
  @Job(name = "Send overdue notifications")
  public void sendOverdueNotifications() {
    List<BookLoan> overdueLoans = bookLoanRepository.findOverdueLoans(ZonedDateTime.now());

    LOG.info("Found {} overdue loans", overdueLoans.size());

    for (BookLoan loan : overdueLoans) {
      if (!notificationRepository.hasNotificationBeenSent(loan, NotificationType.EMAIL)) {
        try {
          createAndSendOverdueNotification(loan);
        }
        catch (Exception e) {
          LOG.error(
            "Error sending overdue notification for loan {}: {}", loan.getId(), e.getMessage(), e);
        }
      }
    }
  }

  @Transactional
  public void createAndSendDueDateReminder(BookLoan loan) {
    // Calculate days until due
    long daysUntilDue =
      java.time.Duration.between(ZonedDateTime.now(), loan.getDueDate()).toDays() + 1;

    // Create subject for email
    String subject =
      String.format(
        "Reminder: '%s' is due in %d days", loan.getBook().getTitle(), daysUntilDue);

    // Create context for email template
    Context context = new Context();
    context.setVariable("loan", loan);
    context.setVariable("daysUntilDue", daysUntilDue);
    context.setVariable(
      "accountUrl", applicationProperties.getApp().getUrl() + "/loans/my-loans");

    // Process the template
    String emailContent = templateEngine.process("emails/due-date-reminder", context);

    // Create notification entity
    Notification notification = new Notification();
    notification.setLoan(loan);
    notification.setType(NotificationType.EMAIL);
    notification.setSubject(subject);
    notification.setContent(emailContent);
    notification.setStatus(NotificationStatus.QUEUED);
    notification.setSentAt(ZonedDateTime.now());

    // Save notification and queue the job to send it
    final Notification updatedNotification = notificationRepository.save(notification);
    jobScheduler.enqueue(() -> sendNotificationById(updatedNotification.getId()));

    LOG.info("Due date reminder created for loan {}", loan.getId());
  }

  @Transactional
  public void createAndSendOverdueNotification(BookLoan loan) {
    // Update loan status to OVERDUE if needed
    if (loan.getStatus() != BookLoan.LoanStatus.OVERDUE) {
      loan.setStatus(BookLoan.LoanStatus.OVERDUE);
      bookLoanRepository.save(loan);
    }

    // Calculate days overdue
    long daysOverdue =
      java.time.Duration.between(loan.getDueDate(), ZonedDateTime.now()).toDays();

    // Create subject for email
    String subject =
      String.format("OVERDUE: '%s' was due %d days ago", loan.getBook().getTitle(), daysOverdue);

    // Create context for email template
    Context context = new Context();
    context.setVariable("loan", loan);
    context.setVariable("daysOverdue", daysOverdue);
    context.setVariable(
      "accountUrl", applicationProperties.getApp().getUrl() + "/loans/my-loans");

    // Process the template
    String emailContent = templateEngine.process("emails/overdue-notification", context);

    // Create notification entity
    Notification notification = new Notification();
    notification.setLoan(loan);
    notification.setType(NotificationType.EMAIL);
    notification.setSubject(subject);
    notification.setContent(emailContent);
    notification.setStatus(NotificationStatus.QUEUED);
    notification.setSentAt(ZonedDateTime.now());

    // Save notification and queue the job to send it
    final Notification updatedNotification = notificationRepository.save(notification);
    jobScheduler.enqueue(() -> sendNotificationById(updatedNotification.getId()));

    LOG.info("Overdue notification created for loan {}", loan.getId());
  }

  @Transactional
  @Job(name = "Schedule daily notification jobs")
  public void scheduleDailyNotificationJobs() {
    jobScheduler.enqueue(() -> sendDueDateReminders());
    jobScheduler.enqueue(() -> sendOverdueNotifications());
  }
}
