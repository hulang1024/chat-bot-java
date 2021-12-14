package bot.eval_server.eval.exn;

import net.mamoe.mirai.event.events.MessageEvent;

import java.util.Map;

public abstract class ExnHandler {
    protected String src;
    protected MessageEvent event;

    public void setContext(String src, MessageEvent event) {
        this.src = src;
        this.event = event;
    }

    public abstract String toReadableText(Map<String, Object> exnData, String error);
}