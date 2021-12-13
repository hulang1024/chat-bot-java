package bot;

public class BotEmoji {
    static String[] hazeTable = new String[]{
        "\uD83D\uDE33", "\uD83E\uDD14", "\uD83E\uDDD0"
    };

    public static String haze() {
        return hazeTable[RandomUtils.getInt(hazeTable.length)];
    }
}