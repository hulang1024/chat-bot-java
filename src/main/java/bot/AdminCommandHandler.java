package bot;

import bot.eval_server.*;
import bot.eval_server.eval.Evaluator;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class AdminCommandHandler {
    private List<Long> adminIds;

    private EnvManager envManager;

    private APIAccess apiAccess;

    public AdminCommandHandler(APIAccess apiAccess) {
        this.apiAccess = apiAccess;
        envManager = new EnvManager(apiAccess);
        adminIds = Arrays.stream(BotMain.config.getProperty("admin.ids").split(","))
            .map(Long::parseLong)
            .collect(Collectors.toList());
    }

    public boolean testAndHandle(String messageString, MessageEvent event) {
        if (!messageString.startsWith("admin")) {
            return false;
        }

        String[] commandParts = messageString.split(" ");
        if (commandParts.length < 2) {
            return false;
        }

        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        messageChainBuilder.add(new QuoteReply(event.getMessage()));

        if (! adminIds.contains(event.getSender().getId())) {
            messageChainBuilder.add("无权限");
            event.getSubject().sendMessage(messageChainBuilder.build());
            return true;
        }

        String command = commandParts[1];
        String[] commandArgs = ArrayUtils.subarray(commandParts, 2, commandParts.length);

        APIResult result = null;
        switch (command) {
            case "eval/enable":
                if (commandArgs.length == 1) {
                    boolean enable = "1".equals(commandArgs[0]);
                    Evaluator.enable = enable;
                    Evaluator.hintDisable = !enable && "2".equals(commandArgs[0]);
                    result = new EvalAPI(apiAccess).enable(enable);
                }
                break;
            case "eval/config":
                if (commandArgs.length == 1) {
                    String[] pair = commandArgs[0].split("=");
                    if ("timeout".equals(pair[0])) {
                        try {
                            int timeout = Integer.parseInt(pair[1]);
                            result = new EvalAPI(apiAccess).config(timeout);
                        } catch (Exception e) {}
                    }
                }
                break;
            case "env/create":
                if (commandArgs.length >= 2) {
                    EnvInfo env = new EnvInfo();
                    env.id = commandArgs[0];
                    String scopeName = commandArgs[1].toLowerCase();
                    env.scope = EnvScope.from(scopeName);
                    boolean valid = true;
                    switch (env.scope) {
                        case Group:
                            if (commandArgs.length == 3) {
                                env.ownerId = Long.parseLong(commandArgs[2]);
                            } else {
                                valid = false;
                            }
                            break;
                        case Private:
                            env.ownerId = Long.parseLong(env.id);
                            break;
                    }
                    if (valid) {
                        result = envManager.createEnv(env);
                    }
                }
                break;
            case "env/change_scope":
                if (commandArgs.length == 3) {
                    String envId = commandArgs[0];
                    String newScopeName = commandArgs[2].toLowerCase();
                    result = envManager.changeEnvScope(envId, EnvScope.from(newScopeName));
                }
                break;
            case "env/remove":
                if (commandArgs.length == 1) {
                    String envId = commandArgs[0];
                    result = envManager.removeEnv(envId);
                }
                break;
            case "env/rename":
                if (commandArgs.length == 2) {
                    String oldEnvId = commandArgs[0];
                    String newEnvId = commandArgs[1];
                    result = envManager.renameEnv(oldEnvId, newEnvId);
                }
                break;
            case "env/reset":
                if (commandArgs.length == 1) {
                    String envId = commandArgs[0];
                    result = envManager.resetEnv(envId);
                }
                break;
            case "env/ids":
                result = envManager.getEnvIds();
                break;
            case "envs":
                StringJoiner joiner = new StringJoiner("\n");
                String format = "%-14s\t%-14s\t%s";
                joiner.add(String.format(format, "Id", "Owner", "Scope"));
                envManager.getEnvs()
                    .stream()
                    .sorted((x, y) -> x.scope.ordinal() - y.scope.ordinal())
                    .forEach(env -> {
                        joiner.add(String.format(format, env.id, env.ownerId, env.scope));
                    });
                result = new APIResult();
                result.data = joiner.toString();
                break;
        }

        String message;
        if (result != null) {
            if (result.isOk()) {
                if (result.data != null) {
                    message = result.data instanceof String
                        ? (String)result.data
                        : APIAccess.gson.toJson(result.data);
                } else {
                    message = "执行成功";
                }
            } else if (StringUtils.isNotEmpty(result.error)) {
                message = result.error;
            } else {
                message = "执行失败";
            }
        } else {
            message = "无效命令";
        }

        messageChainBuilder.add(message);
        event.getSubject().sendMessage(messageChainBuilder.build());
        return true;
    }
}