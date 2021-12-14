package bot.eval_server;

public enum EnvScope {
    Public,
    Group,
    Private;

    public static EnvScope from(String name) {
        switch (name) {
            case "public":
                return EnvScope.Public;
            case "group":
                return EnvScope.Group;
            case "private":
                return EnvScope.Private;
            default:
                return EnvScope.Public;
        }
    }
}