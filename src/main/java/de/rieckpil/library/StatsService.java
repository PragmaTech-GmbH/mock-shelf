package de.rieckpil.library;

import java.time.ZonedDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class StatsService {
  Map<String, Object> getActivityStatistics(ZonedDateTime start, ZonedDateTime end) {

    return null;
  }

  Map<String, Object> getSystemHealth() {
    return null;
  }

  String triggerJob(String jobType) {
    return null;
  }
}
