package bot.eval_server.eval.exn;

import bot.RandomUtils;

import java.util.Map;

public class ReadExnHandler implements ExnHandler {
    @Override
    public String toReadableText(Map<String, Object> exnData, String error) {
        if (error.indexOf("expected a `)` to close `(`") > -1) {
            return "又双叒叕少了右括号？";
        } else {
            return String.format("读取阶段错误\n%s", error);
        }
    }
}