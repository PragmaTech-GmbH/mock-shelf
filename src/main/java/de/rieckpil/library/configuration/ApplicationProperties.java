package de.rieckpil.library.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "mock-shelf")
public class ApplicationProperties {

  @NotNull private final Email email = new Email();

  @NotNull private final Queue queue = new Queue();

  @NotNull private final Isbn isbn = new Isbn();

  @NotNull private final Loan loan = new Loan();

  @NotNull private final App app = new App();

  public Email getEmail() {
    return email;
  }

  public Queue getQueue() {
    return queue;
  }

  public Isbn getIsbn() {
    return isbn;
  }

  public Loan getLoan() {
    return loan;
  }

  public App getApp() {
    return app;
  }

  public static class Email {
    @NotBlank(message = "From email address is required")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
        message = "From email address must be valid")
    private String from = "noreply@mockshelf.com";

    @NotBlank(message = "From name is required")
    private String name = "MockShelf Library";

    public String getFrom() {
      return from;
    }

    public void setFrom(String from) {
      this.from = from;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public static class Queue {
    @NotBlank(message = "Notifications queue name is required")
    private String notifications = "mockshelf.notifications";

    public String getNotifications() {
      return notifications;
    }

    public void setNotifications(String notifications) {
      this.notifications = notifications;
    }
  }

  public static class Isbn {
    @NotNull private final Api api = new Api();

    public Api getApi() {
      return api;
    }

    public static class Api {
      @NotBlank(message = "ISBN API URL is required")
      private String url =
          "https://openlibrary.org/api/books?bibkeys=ISBN:{isbn}&format=json&jscmd=data";

      @Min(value = 1000, message = "API timeout must be at least 1000ms")
      private int timeout = 5000;

      public String getUrl() {
        return url;
      }

      public void setUrl(String url) {
        this.url = url;
      }

      public int getTimeout() {
        return timeout;
      }

      public void setTimeout(int timeout) {
        this.timeout = timeout;
      }
    }
  }

  public static class Loan {
    @Min(value = 1, message = "Reminder days before due must be at least 1")
    private int reminderDaysBeforeDue = 7;

    public int getReminderDaysBeforeDue() {
      return reminderDaysBeforeDue;
    }

    public void setReminderDaysBeforeDue(int reminderDaysBeforeDue) {
      this.reminderDaysBeforeDue = reminderDaysBeforeDue;
    }
  }

  public static class App {
    @NotBlank(message = "Application URL is required")
    @Pattern(regexp = "^(http|https)://.*", message = "Application URL must be a valid URL")
    private String url = "http://localhost:8080";

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }
  }
}
