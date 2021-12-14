package bot.utils;

import java.time.LocalDateTime;

public class DateTimeUtils {
    public static String getSimpleToMinute(LocalDateTime time) {
        return time.toString().substring(5, 16).replace("T", " ");
    }
}