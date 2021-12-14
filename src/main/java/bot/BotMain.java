package bot;

import bot.eval_server.APIAccess;
import bot.message_handlers.AdminCommandHandler;
import bot.message_handlers.EvalLispHandler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.BotConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotMain {
    public static final Properties config = new Properties();
    public static APIAccess evalServerApiAccess;

    public static void main(String[] args) {
        try {
            loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Bot bot = BotFactory.INSTANCE.newBot(
            Long.parseLong(config.getProperty("bot.qq")),
            config.getProperty("bot.password"),
            new BotConfiguration() {{
                fileBasedDeviceInfo();
                setProtocol(MiraiProtocol.ANDROID_PHONE);
                noBotLog();
            }});
        bot.login();

        loop(bot);
    }

    public static void loop(Bot bot) {
        evalServerApiAccess = new APIAccess(config.getProperty("evalServer.host"));

        AdminManager.init();
        AdminCommandHandler adminCommandHandler = new AdminCommandHandler(evalServerApiAccess);
        EvalLispHandler evalLispMessageHandler = new EvalLispHandler(evalServerApiAccess);

        bot.getEventChannel().subscribeAlways(MessageEvent.class, (event) -> {
            String messageString = event.getMessage().contentToString().trim();

            if (adminCommandHandler.testAndHandle(messageString, event)) {
                return;
            } else {
                evalLispMessageHandler.testAndHandle(messageString, event);
            }
        });
    }

    private static void loadConfig() throws IOException {
        File file = new File(System.getProperty("user.dir"), "config.properties");
        try (InputStream in = new FileInputStream(file)) {
            config.load(in);
        }
    }
}