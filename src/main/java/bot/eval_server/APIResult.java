package bot.eval_server;

public class APIResult {
    public int code;
    public Object data;
    public String output;
    public String value;
    public String error;

    public static APIResult ok() {
        return new APIResult();
    }

    public boolean isOk() {
        return code == 0;
    }
}