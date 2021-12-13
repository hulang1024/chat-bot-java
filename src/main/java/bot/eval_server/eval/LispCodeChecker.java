package bot.eval_server.eval;

public class LispCodeChecker {
    public static boolean maybeHasLispCode(String str) {
        char firstCh = str.charAt(0);

        if (firstCh == ';' && str.contains("\n")) {
            return true;
        }

        return (str.startsWith("(") || str.startsWith("（")) &&
            str.charAt(1) != '"' && (str.contains(")") || str.contains("）"));
    }
}