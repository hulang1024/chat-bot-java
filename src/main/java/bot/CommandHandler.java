package bot;

public interface CommandHandler {
    CommandResult onCommand(String[] commandArgs);
}
