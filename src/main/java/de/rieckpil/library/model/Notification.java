package de.rieckpil.library.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "notifications")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "loan_id", nullable = false)
  private BookLoan loan;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private NotificationType type;

  @Column(nullable = false)
  private String subject;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Column(nullable = true)
  private String errorMessage;

  @CreationTimestamp
  @Column(nullable = false)
  private ZonedDateTime sentAt;

  @Enumerated(EnumType.STRING)
  private NotificationStatus status = NotificationStatus.QUEUED;

  public enum NotificationType {
    EMAIL,
    SMS
  }

  public enum NotificationStatus {
    QUEUED,
    SENT,
    FAILED
  }

  public UUID getId() {
    return id;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public  void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public BookLoan getLoan() {
    return loan;
  }

  public void setLoan(BookLoan loan) {
    this.loan = loan;
  }

  public NotificationType getType() {
    return type;
  }

  public void setType(NotificationType type) {
    this.type = type;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public ZonedDateTime getSentAt() {
    return sentAt;
  }

  public void setSentAt(ZonedDateTime sentAt) {
    this.sentAt = sentAt;
  }

  public NotificationStatus getStatus() {
    return status;
  }

  public void setStatus(NotificationStatus status) {
    this.status = status;
  }
}
