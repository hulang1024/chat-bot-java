package bot.scheme;

public class SchemeCodeChecker {
    public static boolean maybeHasSchemeCode(String str) {
        char firstCh = str.charAt(0);

        if (firstCh == ';' && str.contains("\n")) {
            return true;
        }

        return str.startsWith("(") && str.charAt(1) != '"' && str.contains(")");
    }
}