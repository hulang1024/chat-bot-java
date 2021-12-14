package bot.eval_server.eval;

import bot.BotMain;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;
import org.apache.commons.lang3.StringUtils;

import java.io.DataInputStream;
import java.io.File;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputHandler {
    private static Pattern imageIdPattern = Pattern.compile("\\[s:image:id=(\\d+)\\]");
    private static Pattern imageUrlPattern = Pattern.compile("\\[s:image:url=(.+)\\]");
    private static Pattern audioUrlPattern = Pattern.compile("\\[s:audio:url=(.+)\\]");

    public static boolean handle(MessageChainBuilder messageChainBuilder, String output, MessageEvent event) {
        if (StringUtils.isEmpty(output)) {
            return false;
        }
        Matcher matcher = imageIdPattern.matcher(output);
        if (matcher.find()) {
            String filename = matcher.group(1);
            String tempPath = BotMain.config.getProperty("evalServer.tempPath");
            try {
                File file = new File(String.format("%s/%s", tempPath, filename));
                net.mamoe.mirai.message.data.Image image = event.getSubject()
                    .uploadImage(ExternalResource.create(file));
                file.delete();
                messageChainBuilder.add(image);
            } catch (Exception e) {
                return false;
            }
            output = output.replaceAll(imageIdPattern.pattern(), "");
        }
        matcher = imageUrlPattern.matcher(output);
        if (matcher.find()) {
            String fileUrl = matcher.group(1);
            try {
                net.mamoe.mirai.message.data.Image image = event.getSubject()
                    .uploadImage(ExternalResource.create(
                        new DataInputStream(new URL(fileUrl).openStream())));
                messageChainBuilder.add(image);
            } catch (Exception e) {
                return false;
            }
            output = output.replaceAll(imageUrlPattern.pattern(), "");
        }

        matcher = audioUrlPattern.matcher(output);
        if (matcher.find() && event instanceof GroupMessageEvent) {
            Group group = ((GroupMessageEvent) event).getGroup();
            String fileUrl = matcher.group(1);
            try {
                net.mamoe.mirai.message.data.Audio audio = group.uploadAudio(ExternalResource.create(
                        new DataInputStream(new URL(fileUrl).openStream())));
                messageChainBuilder.add(audio);
            } catch (Exception e) {
                return false;
            }
            output = output.replaceAll(audioUrlPattern.pattern(), "");
        }

        messageChainBuilder.add(StringUtils.trim(output));
        return true;
    }

    public static boolean isBigString(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        return str.length() > 800 || countLine(str) > 30;
    }

    public static int countLine(String str) {
        int lineCount = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\n' || c == '\r') {
                lineCount++;
            }
        }
        return lineCount;
    }
}