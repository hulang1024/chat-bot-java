package bot.eval_server;

import java.util.HashMap;
import java.util.Map;

public class EvalAPI {
    public APIAccess apiAccess;

    public EvalAPI(APIAccess apiAccess) {
        this.apiAccess = apiAccess;
    }

    public APIResult config(int timeout) {
        Map<String, Object> params = new HashMap<>();
        params.put("timeout", timeout);
        return apiAccess.perform("eval/config", params);
    }

    public APIResult enable(boolean value) {
        Map<String, Object> params = new HashMap<>();
        params.put("enable", value ? 1 : 0);
        return apiAccess.perform("eval/enable", params);
    }
}