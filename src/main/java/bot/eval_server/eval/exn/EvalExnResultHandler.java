package bot.eval_server.eval.exn;

import bot.eval_server.APIResult;
import net.mamoe.mirai.event.events.MessageEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EvalExnResultHandler {
    private static Map<String, ExnHandler> exnHandlerMap = new HashMap(){{
        put("read", new ReadExnHandler());
        put("syntax", new SyntaxExnHandler());
        put("variable", new VariableExnHandler());
        put("out-of-memory", new OutOfMemoryExnHandler());
    }};

    public static boolean isOutOfMemory(APIResult result) {
        if (result.data instanceof Map) {
            return StringUtils.equals("out-of-memory", (String)((Map) result.data).get("type"));
        } else {
            return false;
        }
    }

    public static String toReadableText(APIResult result, String src, MessageEvent event) {
        if (result.data instanceof Map) {
            Map<String, Object> exnData = (Map) result.data;

            String type = (String) exnData.get("type");
            return Optional.ofNullable(exnHandlerMap.get(type))
                .map(exnHandler -> {
                    exnHandler.setContext(src, event);
                    return exnHandler.toReadableText(exnData, result.error);
                })
                .orElseGet(() -> result.error);
        } else {
            return result.error;
        }
    }
}