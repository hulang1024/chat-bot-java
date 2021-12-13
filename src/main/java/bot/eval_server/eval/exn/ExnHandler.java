package bot.eval_server.eval.exn;

import java.util.Map;

public interface ExnHandler {
    String toReadableText(Map<String, Object> exnData, String error, String src);
}