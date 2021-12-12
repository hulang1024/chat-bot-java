package bot.eval_server.eval;

import bot.eval_server.APIAccess;
import bot.eval_server.APIResult;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class Evaluator {
    public static boolean enable = true;
    public static boolean hintDisable = false;

    public APIAccess apiAccess;

    public Evaluator(APIAccess apiAccess) {
        this.apiAccess = apiAccess;
    }

    public APIResult eval(String expr, String envId, User sender, Group group) {
        Map<String, Object> senderInfo = new HashMap<>();
        senderInfo.put("id", sender.getId());
        senderInfo.put("nickname", sender.getNick());
        senderInfo.put("avatarUrl", sender.getAvatarUrl());

        Map<String, Object> params = new HashMap<>();
        params.put("expr", expr);
        params.put("env_id", envId);
        params.put("sender", senderInfo);

        if (group != null) {
            Map<String, Object> groupInfo = new HashMap<>();
            groupInfo.put("id", group.getId());
            groupInfo.put("name", group.getName());

            params.put("group", groupInfo);
        }

        APIResult result = apiAccess.perform("eval", params);
        if (!result.isOk() && StringUtils.isEmpty(result.error)) {
            result.error = "\uD83D\uDC7B程序执行请求好像发生异常了哦，请重试。";
        }
        return result;
    }
}