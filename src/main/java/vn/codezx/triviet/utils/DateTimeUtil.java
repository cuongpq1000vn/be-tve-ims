package vn.codezx.triviet.utils;

import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTimeUtil {
    private DateTimeUtil() {}

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
    public static final DateTimeFormatter timeToCodeFormatter = DateTimeFormatter.ofPattern("HHmm");

    public static Date startOfDay() {
        Calendar startOfDay = new GregorianCalendar();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        return startOfDay.getTime();
    }

    public static Date endOfDay() {
        Calendar endOfDay = new GregorianCalendar();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 999);

        return endOfDay.getTime();
    }
}
