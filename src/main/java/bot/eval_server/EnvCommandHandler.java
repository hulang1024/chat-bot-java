package bot.eval_server;

import bot.CommandHandler;
import bot.CommandResult;
import bot.eval_server.eval.Evaluator;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.StringJoiner;

public class EnvCommandHandler implements CommandHandler {
    private EvalAPI evalAPI;
    private EnvManager envManager;

    public EnvCommandHandler(APIAccess apiAccess) {
        evalAPI = new EvalAPI(apiAccess);
        envManager = new EnvManager(apiAccess);
    }

    @Override
    public CommandResult onCommand(String[] commandArgs) {
        CommandResult result = new CommandResult();
        switch (commandArgs[0]) {
            case "enable":
                if (commandArgs.length == 1) {
                    boolean enable = "1".equals(commandArgs[0]);
                    Evaluator.enable = enable;
                    Evaluator.hintDisable = !enable && "2".equals(commandArgs[0]);
                    toCommandResult(result, evalAPI.enable(enable));
                }
                break;
            case "config":
                if (commandArgs.length == 1) {
                    String[] pair = commandArgs[0].split("=");
                    if ("timeout".equals(pair[0])) {
                        int timeout = Integer.parseInt(pair[1]);
                        toCommandResult(result, evalAPI.config(timeout));
                    }
                }
                break;
            case "create":
                create(result, commandArgs);
                break;
            case "change_scope":
                changeEnvScope(result, commandArgs);
                break;
            case "remove":
                if (commandArgs.length == 1) {
                    String envId = commandArgs[0];
                    toCommandResult(result, envManager.removeEnv(envId));
                }
                break;
            case "rename":
                if (commandArgs.length == 2) {
                    String oldEnvId = commandArgs[0];
                    String newEnvId = commandArgs[1];
                    toCommandResult(result, envManager.renameEnv(oldEnvId, newEnvId));
                }
                break;
            case "reset":
                if (commandArgs.length == 1) {
                    String envId = commandArgs[0];
                    toCommandResult(result, envManager.resetEnv(envId));
                }
                break;
            case "ids":
                toCommandResult(result, envManager.getEnvIds());
                break;
            case "list":
                list(result, commandArgs);
                break;
            default:
                break;
        }

        return result;
    }

    private void create(CommandResult result, String[] commandArgs) {
        if (commandArgs.length < 2) {
            return;
        }
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
            toCommandResult(result, envManager.createEnv(env));
        }
    }

    private void changeEnvScope(CommandResult result, String[] commandArgs) {
        if (commandArgs.length < 2) {
            return;
        }

        String envId = commandArgs[0];
        if (!envManager.getEnv(envId).isPresent()) {
            return;
        }

        EnvInfo env = envManager.getEnv(envId).get();
        EnvScope newScope = EnvScope.from(commandArgs[1].toLowerCase());
        if (newScope == null) {
            return;
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
                        return;
                    }
                } else {
                    return;
                }
                break;
        }

        env.scope = newScope;
        result.ok();
    }

    private void list(CommandResult result, String[] commandArgs) {
        StringJoiner joiner = new StringJoiner("\n");
        String format = "%-14s\t%-14s\t%s";
        joiner.add(String.format(format, "Id", "Owner", "Scope"));
        envManager.getEnvs()
                .stream()
                .sorted(Comparator.comparingInt(x -> x.scope.ordinal()))
                .forEach(env ->
                        joiner.add(String.format(format, env.id, env.ownerId, env.scope)));
        result.ok();
        result.message = joiner.toString();
    }

    private CommandResult toCommandResult(CommandResult commandResult, APIResult apiResult) {
        if (apiResult.isOk()) {
            commandResult.ok();
            if (apiResult.data != null) {
                commandResult.message = apiResult.data instanceof String
                        ? (String)apiResult.data
                        : APIAccess.gson.toJson(apiResult.data);
            }
        } else if (StringUtils.isNotEmpty(apiResult.error)) {
            commandResult.fail();
            commandResult.message = apiResult.error;
        }
        return commandResult;
    }
}
