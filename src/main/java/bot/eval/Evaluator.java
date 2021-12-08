package bot.eval;

import com.google.gson.Gson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Evaluator {
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();
    private static final Moshi moshi = new Moshi.Builder().build();
    private static final JsonAdapter<EvalResult> httpAPIResultAdapter = moshi.adapter(EvalResult.class);

    private String host;

    public Evaluator(String host) {
        this.host = host;
    }

    public EvalResult eval(String expr, User sender, Group group) {
        Map<String, Object> senderInfo = new HashMap<>();
        senderInfo.put("id", sender.getId());
        senderInfo.put("nickname", sender.getNick());
        senderInfo.put("avatarUrl", sender.getAvatarUrl());

        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("expr", expr);
        reqParams.put("sender", senderInfo);

        if (group != null) {
            Map<String, Object> groupInfo = new HashMap<>();
            groupInfo.put("id", group.getId());
            groupInfo.put("name", group.getName());

            reqParams.put("group", groupInfo);
        }

        RequestBody body = RequestBody.create(gson.toJson(reqParams), JSON);
        Request request = new Request.Builder().url(host).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return httpAPIResultAdapter.fromJson(response.body().source());
        } catch (IOException e) {
            e.printStackTrace();
            EvalResult result = new EvalResult();
            result.error = "\uD83D\uDC7B程序执行请求好像发生异常了哦，请重试。";
            return result;
        }
    }
}