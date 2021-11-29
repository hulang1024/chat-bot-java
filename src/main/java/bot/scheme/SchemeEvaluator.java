package bot.scheme;

import com.google.gson.Gson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import net.mamoe.mirai.contact.User;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SchemeEvaluator {
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final static Gson gson = new Gson();
    private final Moshi moshi = new Moshi.Builder().build();
    private final JsonAdapter<EvalResult> httpAPIResultAdapter = moshi.adapter(EvalResult.class);

    public EvalResult eval(String host, String expr, User sender) {
        Map<String, Object> senderInfo = new HashMap<>();
        senderInfo.put("id", sender.getId());
        senderInfo.put("nickname", sender.getNick());
        senderInfo.put("avatarUrl", sender.getAvatarUrl());

        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("expr", expr);
        reqParams.put("sender", senderInfo);

        RequestBody body = RequestBody.create(gson.toJson(reqParams), JSON);
        Request request = new Request.Builder().url(host).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return httpAPIResultAdapter.fromJson(response.body().source());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new EvalResult();
    }
}