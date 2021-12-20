package bot.block;

import bot.CommandHandler;
import bot.CommandResult;
import bot.utils.DateTimeUtils;
import bot.utils.DurationExpr;

import java.time.LocalDateTime;
import java.util.StringJoiner;

public class BlockCommandHandler implements CommandHandler {
    @Override
    public CommandResult onCommand(String[] commandArgs) {
        if (commandArgs.length < 1) {
            return null;
        }

        CommandResult result = new CommandResult();

        switch (commandArgs[0]) {
            case "list":
                list(result);
                break;
            case "+":
            case "add":
                add(result, commandArgs);
                break;
            case "-":
            case "remove":
                remove(result, commandArgs);
                break;
            case "clear":
                BlockUserManager.clear();
                result.ok();
            default:
                break;
        }
        
        return result;
    }

    private void list(CommandResult result) {
        StringJoiner joiner = new StringJoiner("\n");
        String format = "%-14s\t%-14s\t%s";
        joiner.add(String.format(format, "Id", "StartTime", "Minutes"));
        BlockUserManager.getAll().forEach(blockUser ->
                joiner.add(String.format(format,
                        blockUser.getId(),
                        DateTimeUtils.getSimpleToMinute(blockUser.startTime),
                        blockUser.minutes)));
        result.ok();
        result.message = joiner.toString();
    }

    private void add(CommandResult result, String[] commandArgs) {
        if (commandArgs.length != 3) {
            return;
        }
        long userId = Long.parseLong(commandArgs[1]);
        int minutes = DurationExpr.parseToMinutes(commandArgs[2], 30);
        BlockUser blockUser = new BlockUser(userId);
        blockUser.startTime = LocalDateTime.now();
        blockUser.minutes = minutes;
        BlockUserManager.add(blockUser);
        result.ok();
    }

    private void remove(CommandResult result, String[] commandArgs) {
        if (commandArgs.length != 2) {
            return;
        }
        long userId = Long.parseLong(commandArgs[1]);
        BlockUserManager.remove(userId);
        result.ok();
    }
}
