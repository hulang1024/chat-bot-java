package bot.message_handlers;

import bot.AdminManager;
import bot.block.BlockUser;
import bot.block.BlockUserManager;
import bot.eval_server.*;
import bot.eval_server.eval.Evaluator;
import bot.utils.DateTimeUtils;
import bot.utils.DurationExpr;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.StringJoiner;

public class AdminCommandHandler {

    private EnvManager envManager;

    private APIAccess apiAccess;

    public AdminCommandHandler(APIAccess apiAccess) {
        this.apiAccess = apiAccess;
        envManager = new EnvManager(apiAccess);
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
                        int timeout = Integer.parseInt(pair[1]);
                        result = new EvalAPI(apiAccess).config(timeout);
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
                result = onChangeEnvScope(commandArgs);
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
            case "envs": {
                StringJoiner joiner = new StringJoiner("\n");
                String format = "%-14s\t%-14s\t%s";
                joiner.add(String.format(format, "Id", "Owner", "Scope"));
                envManager.getEnvs()
                        .stream()
                        .sorted(Comparator.comparingInt(x -> x.scope.ordinal()))
                        .forEach(env ->
                            joiner.add(String.format(format, env.id, env.ownerId, env.scope)));
                result = new APIResult();
                result.data = joiner.toString();
                break;
            }

            case "block": {
                if (commandArgs.length >= 1) {
                    String action = commandArgs[0];

                    switch (action) {
                        case "list": {
                            StringJoiner joiner = new StringJoiner("\n");
                            String format = "%-14s\t%-14s\t%s";
                            joiner.add(String.format(format, "Id", "StartTime", "Minutes"));
                            BlockUserManager.getAll().forEach(blockUser ->
                                joiner.add(String.format(format,
                                    blockUser.getId(),
                                    DateTimeUtils.getSimpleToMinute(blockUser.startTime),
                                    blockUser.minutes)));
                            result = new APIResult();
                            result.data = joiner.toString();
                            break;
                        }
                        case "+":
                        case "add":
                            if (commandArgs.length == 3) {
                                long userId = Long.parseLong(commandArgs[1]);
                                int minutes = DurationExpr.parseToMinutes(commandArgs[2], 30);
                                BlockUser blockUser = new BlockUser(userId);
                                blockUser.startTime = LocalDateTime.now();
                                blockUser.minutes = minutes;
                                BlockUserManager.add(blockUser);
                                result = APIResult.ok();
                            }
                            break;
                        case "-":
                        case "remove":
                            if (commandArgs.length == 2) {
                                long userId = Long.parseLong(commandArgs[1]);
                                BlockUserManager.remove(userId);
                                result = APIResult.ok();
                                break;
                            }
                        case "clear":
                            BlockUserManager.clear();
                            result = APIResult.ok();
                    }
                }
            }
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

    private APIResult onChangeEnvScope(String[] commandArgs) {
        if (commandArgs.length < 2) {
            return null;
        }

        String envId = commandArgs[0];
        if (!envManager.getEnv(envId).isPresent()) {
            return null;
        }

        EnvInfo env = envManager.getEnv(envId).get();
        EnvScope newScope = EnvScope.from(commandArgs[1].toLowerCase());
        if (newScope == null) {
            return null;
        }

        switch (newScope) {
            case Public:
                env.ownerId = 0;
                break;
            case Group:
            case Private:
                if (commandArgs.length == 3) {
                    try {
                        env.ownerId = Long.parseLong(commandArgs[2]);
                    } catch (Exception e) {
                        return null;
                    }
                } else {
                    return null;
                }
                break;
        }

        env.scope = newScope;
        return APIResult.ok();
    }
}