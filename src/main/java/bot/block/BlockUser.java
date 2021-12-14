package bot.block;

import net.mamoe.mirai.contact.User;

import java.time.LocalDateTime;

public class BlockUser {
    private Long id;
    public LocalDateTime startTime;
    public Integer minutes;

    public BlockUser(long id) {
        this.id = id;
    }

    public BlockUser(User user) {
        this(user.getId());
    }

    public long getId() {
        return id;
    }
}
