package de.rieckpil.library.configuration;

import de.rieckpil.library.NotificationService;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.context.annotation.Configuration;

import static org.jobrunr.scheduling.cron.Cron.daily;

@Configuration
public class JobRunrConfiguration {

  private final NotificationService notificationService;

  public JobRunrConfiguration(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  public void scheduleRecurringJobs(JobScheduler jobScheduler) {
    // Schedule the daily notification jobs to run at 1:00 AM every day
    jobScheduler.scheduleRecurrently(
        "daily-notification-scheduler",
        daily(1, 0),
        notificationService::scheduleDailyNotificationJobs);
  }
}
