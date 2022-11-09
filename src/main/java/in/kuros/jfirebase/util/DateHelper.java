package in.kuros.jfirebase.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

public class DateHelper {
  private DateHelper() {

  }

  public static Date toDate(LocalDateTime localDateTime) {
    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
  }

  public static Date toDate(LocalDate localDate) {
    return Date.from(localDate.atStartOfDay(ZoneOffset.UTC).toInstant());
  }

  public static LocalDate toLocalDate(Date date) {
    return date.toInstant()
        .atZone(ZoneId.of("UTC"))
        .toLocalDate();
  }

  public static LocalDateTime toLocalDateTime(Date date) {
    return Instant.ofEpochMilli(date.getTime())
        .atZone(ZoneId.of("UTC"))
        .toLocalDateTime();
  }
}
