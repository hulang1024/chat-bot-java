package bot.eval_server.eval.exn;

import bot.BotEmoji;

import java.util.Map;

public class VariableExnHandler implements ExnHandler {
    @Override
    public String toReadableText(Map<String, Object> exnData, String error, String src) {
        String id = (String)exnData.get("id");
        return String.format("%s【%s】是什么意思？%s", BotEmoji.haze(), id, Guess.wideBracket(id));
    }
}