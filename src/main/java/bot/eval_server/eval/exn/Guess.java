package bot.eval_server.eval.exn;

public class Guess {
    public static String wideBracket(String contextText) {
        return wideBracket(contextText, true);
    }

    public static String wideBracket(String contextText, boolean containMode) {
        if (containMode
            ? (contextText.contains("（") || contextText.contains("）"))
            : (contextText.startsWith("（") || contextText.startsWith("）"))) {
            return "\n是不是把英文括号`()`打成中文括号`（）`了？";
        } else {
            return "";
        }
    }
}