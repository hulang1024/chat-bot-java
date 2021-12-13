package bot.eval_server.eval.exn;

import java.util.Map;

public class ReadExnHandler implements ExnHandler {
    @Override
    public String toReadableText(Map<String, Object> exnData, String error, String src) {
        if (error.indexOf("expected a `)` to close `(`") > -1) {
            return "又双叒叕少了右括号？" + Guess.wideBracket(src);
        } else {
            return String.format("读取阶段错误\n%s", error);
        }
    }
}