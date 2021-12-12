package bot.eval_server;

import com.google.gson.Gson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import okhttp3.*;

import java.util.Map;

public class APIAccess {
    public static final Gson gson = new Gson();
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Moshi moshi = new Moshi.Builder().build();

    private static final JsonAdapter<APIResult> APIResultAdapter = moshi.adapter(APIResult.class);

    private String host;

    public APIAccess(String host) {
        this.host = host;
    }

    public APIResult perform(String path, Map<String, Object> params) {
        params.put("path", path);
        RequestBody body = RequestBody.create(gson.toJson(params), JSON);
        Request request = new Request.Builder().url(host).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return APIResultAdapter.fromJson(response.body().source());
        } catch (Exception e) {
            e.printStackTrace();
            APIResult result = new APIResult();
            result.code = 1;
            return result;
        }
    }
}