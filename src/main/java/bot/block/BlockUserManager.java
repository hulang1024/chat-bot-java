package bot.block;

import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BlockUserManager {
    private static Map<Long, BlockUser> blockUserMap = new HashMap<>();

    public static void add(BlockUser blockUser) {
        blockUserMap.put(blockUser.getId(), blockUser);
    }

    public static void remove(long userId) {
        blockUserMap.remove(userId);
    }

    public static void clear() {
        blockUserMap.clear();
    }

    public static boolean checkNowIsBlock(long userId) {
        if (!blockUserMap.containsKey(userId)) {
            return false;
        }
        BlockUser blockUser = blockUserMap.get(userId);
        boolean isTimeout = LocalDateTime.now().isAfter(blockUser.startTime.plusMinutes(blockUser.minutes));
        if (isTimeout) {
            remove(userId);
        }
        return !isTimeout;
    }

    public static Collection<BlockUser> getAll() {
        return blockUserMap.values();
    }

    public static void addMessage(MessageChainBuilder builder, BlockUser blockUser) {
        builder.add("\n对 ");
        builder.add(new At(blockUser.getId()));
        builder.add(String.format(" 拒绝服务%s分钟。", blockUser.minutes));
    }
}