package de.rieckpil.library;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import de.rieckpil.library.model.BookLoan;
import de.rieckpil.library.model.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  /** Find all notifications for a specific loan */
  List<Notification> findByLoan(BookLoan loan);

  /** Find notifications by type (EMAIL, SMS, etc.) */
  List<Notification> findByType(Notification.NotificationType type);

  /** Find notifications by status (QUEUED, SENT, FAILED) */
  List<Notification> findByStatus(Notification.NotificationStatus status);

  /** Find all notifications for a specific user */
  @Query("SELECT n FROM Notification n WHERE n.loan.user.id = :userId")
  List<Notification> findByUserId(@Param("userId") UUID userId);

  /** Find notifications sent during a specific time period */
  @Query("SELECT n FROM Notification n WHERE n.sentAt BETWEEN :fromDate AND :toDate")
  List<Notification> findBySentAtBetween(
      @Param("fromDate") ZonedDateTime fromDate, @Param("toDate") ZonedDateTime toDate);

  /** Count notifications sent since a given date */
  @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = 'SENT' AND n.sentAt >= :since")
  Long countSentNotificationsSince(@Param("since") ZonedDateTime since);

  /** Count failed notifications since a given date */
  @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = 'FAILED' AND n.sentAt >= :since")
  Long countFailedNotificationsSince(@Param("since") ZonedDateTime since);

  /** Check if a specific type of notification has been sent for a loan */
  @Query(
      "SELECT COUNT(n) > 0 FROM Notification n WHERE n.loan = :loan AND n.type = :type AND n.status = 'SENT'")
  boolean hasNotificationBeenSent(
      @Param("loan") BookLoan loan, @Param("type") Notification.NotificationType type);

  /** Find the most recent notification for a loan */
  @Query("SELECT n FROM Notification n WHERE n.loan = :loan ORDER BY n.sentAt DESC")
  List<Notification> findRecentNotificationsForLoan(
      @Param("loan") BookLoan loan, Pageable pageable);

  /** Delete old notifications (for maintenance/cleanup) */
  @Query("DELETE FROM Notification n WHERE n.sentAt < :before")
  void deleteNotificationsOlderThan(@Param("before") ZonedDateTime before);
}
