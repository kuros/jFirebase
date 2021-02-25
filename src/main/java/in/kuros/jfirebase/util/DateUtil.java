package in.kuros.jfirebase.util;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static Date getDateWithoutTime(@Nullable final Date date) {
        if (date == null) {
            return null;
        }
        return getCalWithoutTime(date).getTime();
    }

    public static Calendar getCalWithoutTime(@CheckForNull final Date date) {
        if (date == null) {
            return null;
        }
        final Calendar cal = toCalendar(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        clipToStartOfHour(cal);
        return cal;
    }

    public static void clipToStartOfHour(final Calendar calendar) {
        if (calendar != null) {
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
    }

    public static Calendar toCalendar(final Date date) {
        Calendar calendar = null;

        if (date != null) {
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        }

        return calendar;
    }
}
