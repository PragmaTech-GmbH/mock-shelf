package de.rieckpil.library;

import java.util.List;
import java.util.UUID;

import de.rieckpil.library.model.Notification;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
  List<Notification> filterNotifications(String status, String type, int page, int size) {
    return null;
  }

  List<Notification> getAllNotifications(int page, int size) {
    return null;
  }

  Notification resendNotification(UUID id) {
    return null;
  }
}
