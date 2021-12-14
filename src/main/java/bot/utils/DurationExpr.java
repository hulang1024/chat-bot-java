package bot.utils;

public class DurationExpr {
    public static int parseToMinutes(String durationExp, int defaultValue) {
        int minutes = defaultValue;
        if (!durationExp.matches("^\\d+[MmHhDd]$")) {
            return minutes;
        }
        int duration = Integer.parseInt(durationExp.substring(0, durationExp.length() - 1));
        String unit = durationExp.substring(durationExp.length() - 1).toLowerCase();
        switch (unit) {
            case "m":
                minutes = duration;
                break;
            case "h":
                minutes = duration * 60;
                break;
            case "d":
                minutes = duration * 60 * 24;
                break;
        }
        return minutes;
    }
}
