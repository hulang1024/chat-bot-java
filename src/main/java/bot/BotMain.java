package bot;

import bot.scheme.OutputHandler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.BotConfiguration;
import org.apache.commons.lang3.StringUtils;
import bot.scheme.EvalResult;
import bot.scheme.SchemeEvaluator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotMain {
    public static final Properties config = new Properties();

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
        SchemeEvaluator schemeEvaluator = new SchemeEvaluator();

        String schemeHost = config.getProperty("scheme.host");
        String commandPrefixWithQuoteReply = "#";
        String commandPrefixWithoutQuoteReply1 = "!";
        String commandPrefixWithoutQuoteReply2 = "！";

        bot.getEventChannel().subscribeAlways(MessageEvent.class, (event) -> {
            String messageString = event.getMessage().contentToString().trim();

            String expr = "";
            boolean hasQuoteReply = true;

            if (messageString.startsWith("(") && messageString.charAt(1) != '"' && messageString.contains(")")) {
                hasQuoteReply = true;
                expr = messageString;
            } else if (messageString.startsWith(commandPrefixWithoutQuoteReply1)
                || messageString.startsWith(commandPrefixWithoutQuoteReply2)) {
                expr = messageString.substring(commandPrefixWithoutQuoteReply1.length());
                hasQuoteReply = false;
            } else {
                if (messageString.startsWith(commandPrefixWithQuoteReply) && messageString.charAt(1) != '<') {
                    hasQuoteReply = true;
                    expr = messageString.substring(commandPrefixWithQuoteReply.length());
                } else {
                    return;
                }
            }

            Group group = null;
            if (event instanceof GroupMessageEvent) {
                group = ((GroupMessageEvent) event).getGroup();
            }

            EvalResult evalResult = schemeEvaluator.eval(schemeHost, expr, event.getSender(), group);

            MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
            if (hasQuoteReply) {
                messageChainBuilder.add(new QuoteReply(event.getMessage()));
            }

            if (StringUtils.isNotEmpty(evalResult.error)) {
                messageChainBuilder.add("\uD83C\uDF88 " + evalResult.error);
            } else {
                boolean hasOutput = OutputHandler.handle(messageChainBuilder, evalResult.output, event);
                if (StringUtils.isNotEmpty(evalResult.value)) {
                    if (!hasOutput || !"#<void>".equals(evalResult.value)) {
                        if (hasOutput) {
                            messageChainBuilder.add("\n"); // output后面加换行
                        }
                        messageChainBuilder.add(evalResult.value);
                    }
                }
            }

            event.getSubject().sendMessage(messageChainBuilder.build());
        });
    }

    private static void loadConfig() throws IOException {
        File file = new File(System.getProperty("user.dir"), "config.properties");
        try (InputStream in = new FileInputStream(file)) {
            config.load(in);
        }
    }
}