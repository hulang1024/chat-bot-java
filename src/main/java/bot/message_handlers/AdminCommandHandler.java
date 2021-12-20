package bot.message_handlers;

import bot.AdminManager;
import bot.CommandHandler;
import bot.CommandResult;
import bot.block.BlockCommandHandler;
import bot.eval_server.APIAccess;
import bot.eval_server.EnvCommandHandler;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class AdminCommandHandler {

    private static Map<String, CommandHandler> commandHandlerMap;


    private APIAccess apiAccess;

    public AdminCommandHandler(APIAccess apiAccess) {
        this.apiAccess = apiAccess;

        commandHandlerMap = new HashMap<String, CommandHandler>(){{
            put("env", new EnvCommandHandler(apiAccess));
            put("block", new BlockCommandHandler());
        }};
    }

    public boolean testAndHandle(String messageString, MessageEvent event) {
        if (!messageString.startsWith("admin")) {
            return false;
        }

        String[] commandParts = messageString.split("\\s");
        if (commandParts.length < 2) {
            return false;
        }

        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        messageChainBuilder.add(new QuoteReply(event.getMessage()));

        if (! AdminManager.isAdmin(event.getSender())) {
            messageChainBuilder.add("无权限");
            event.getSubject().sendMessage(messageChainBuilder.build());
            return true;
        }

        String command = commandParts[1];
        String[] commandArgs = ArrayUtils.subarray(commandParts, 2, commandParts.length);
        CommandResult result = null;
        if (commandHandlerMap.containsKey(command)) {
            result = commandHandlerMap.get(command).onCommand(commandArgs);
        }

        String message;
        if (result != null) {
            if (result.isOk()) {
                if (StringUtils.isEmpty(result.message)) {
                    message = "ok";
                } else {
                    message = result.message;
                }
            } else {
                message = "执行失败";
                if (StringUtils.isNotEmpty(result.message)) {
                    message += "\n" + result.message;
                }
            }
        } else {
            message = "无效命令";
        }

        messageChainBuilder.add(message);
        event.getSubject().sendMessage(messageChainBuilder.build());
        return true;
    }
}