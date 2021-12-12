package bot.eval_server;

import java.util.*;

public class EnvManager {
    public APIAccess apiAccess;

    private static Set<EnvInfo> envs = new HashSet<>();

    public static final String GLOBAL_ENV_ID = "global";

    public EnvManager(APIAccess apiAccess) {
        this.apiAccess = apiAccess;

        EnvInfo global = new EnvInfo();
        global.id = GLOBAL_ENV_ID;
        global.scope = EnvScope.Public;
        envs.add(global);
    }

    public Optional<EnvInfo> getEnv(String id) {
        return envs.stream().filter((env) -> env.id.equals(id)).findAny();
    }

    public APIResult createEnv(EnvInfo env) {
        Map<String, Object> params = new HashMap<>();
        params.put("env_id", env.id);
        APIResult result = apiAccess.perform("env/create", params);
        if (result.isOk()) {
            envs.add(env);
        } else if (result.code == 2) {
            if (!envs.contains(env)) {
                envs.add(env);
                result.code = 0;
            }
        }
        return result;
    }

    public APIResult removeEnv(String id) {
        envs.removeIf((env) -> env.id.equals(id));

        Map<String, Object> params = new HashMap<>();
        params.put("env_id", id);
        return apiAccess.perform("env/remove", params);
    }

    public APIResult renameEnv(String oldId, String newId) {
        getEnv(oldId).ifPresent((env) -> {
            env.id = newId;
        });
        Map<String, Object> params = new HashMap<>();
        params.put("old_env_id", oldId);
        params.put("new_env_id", newId);
        return apiAccess.perform("env/rename", params);
    }

    public APIResult resetEnv(String id) {
        Map<String, Object> params = new HashMap<>();
        params.put("env_id", id);
        return apiAccess.perform("env/reset", params);
    }

    public APIResult changeEnvScope(String id, EnvScope newScope) {
        getEnv(id).ifPresent((env) -> {
            env.scope = newScope;
        });
        return APIResult.ok();
    }

    public Set<EnvInfo> getEnvs() {
        return envs;
    }

    public APIResult getEnvIds() {
        Map<String, Object> params = new HashMap<>();
        return apiAccess.perform("env/ids", params);
    }
}