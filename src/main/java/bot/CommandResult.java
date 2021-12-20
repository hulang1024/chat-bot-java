package bot;

public class CommandResult {
    public int code = 1;
    public String message;

    public CommandResult ok() {
        code = 0;
        return this;
    }

    public CommandResult fail() {
        code = 1;
        return this;
    }

    public boolean isOk() {
        return code == 0;
    }
}
