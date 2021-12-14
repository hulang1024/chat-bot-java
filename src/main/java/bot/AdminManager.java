package bot;

import net.mamoe.mirai.contact.User;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminManager {
    private static List<Long> adminIds;

    public static void init() {
        adminIds = Arrays.stream(BotMain.config.getProperty("admin.ids").split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public static boolean isAdmin(User user) {
        return adminIds.contains(user.getId());
    }
}
