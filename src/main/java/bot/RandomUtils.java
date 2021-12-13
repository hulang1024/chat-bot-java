package bot;

public class RandomUtils {
    public static int getInt(int max) {
        return (int)Math.floor(Math.random() * max);
    }
}