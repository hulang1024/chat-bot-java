package bot.eval_server;

import java.util.Objects;

public class EnvInfo {
    public String id;
    public long ownerId;
    public EnvScope scope;

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        EnvInfo envInfo = (EnvInfo) o;
        return Objects.equals(id, envInfo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}