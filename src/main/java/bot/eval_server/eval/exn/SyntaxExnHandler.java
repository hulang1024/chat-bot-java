package bot.eval_server.eval.exn;

import java.util.Map;

public class SyntaxExnHandler implements ExnHandler {
    @Override
    public String toReadableText(Map<String, Object> exnData, String error, String src) {
        return String.format("语法错误了哦\n%s", error);
    }
}