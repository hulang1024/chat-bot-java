package bot;

import bot.utils.RandomUtils;

public class BotEmoji {
    private static final String[] hazeTable = new String[]{
        "\uD83D\uDE33", "\uD83E\uDD14", "\uD83E\uDDD0"
    };

    public static final String evalError = "\uD83C\uDF88 ";

    public static String haze() {
        return hazeTable[RandomUtils.getInt(hazeTable.length)];
    }
}