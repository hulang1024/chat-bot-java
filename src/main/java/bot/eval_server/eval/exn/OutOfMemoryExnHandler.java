package bot.eval_server.eval.exn;

import java.util.Map;

public class OutOfMemoryExnHandler implements ExnHandler {
    @Override
    public String toReadableText(Map<String, Object> exnData, String error) {
        return "内存不够了";
    }
}