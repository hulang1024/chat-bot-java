package bot;

import bot.eval_server.*;
import bot.eval_server.eval.Evaluator;
import bot.eval_server.eval.LispCodeChecker;
import bot.eval_server.eval.OutputHandler;
import bot.eval_server.eval.exn.EvalExnResultHandler;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EvalLispMessageHandler {
    private static String commandPrefixWithQuoteReply = "#";
    private static String commandPrefixWithoutQuoteReply1 = "!";
    private static String commandPrefixWithoutQuoteReply2 = "！";

    private static String errorPrefixMessage = "\uD83C\uDF88 ";

    private static Pattern useEnvPattern = Pattern.compile("#env\\s+([\\w-]+)");

    private Evaluator evaluator;

    private EnvManager envManager;

    public EvalLispMessageHandler(APIAccess apiAccess) {
        evaluator = new Evaluator(apiAccess);
        envManager = new EnvManager(apiAccess);
    }

    public boolean testAndHandle(String messageString, MessageEvent event) {
        String expr = "";
        boolean hasQuoteReply = true;

        if (LispCodeChecker.maybeHasLispCode(messageString)) {
            hasQuoteReply = false;
            expr = messageString;
        } else if (messageString.startsWith(commandPrefixWithoutQuoteReply1)
            || messageString.startsWith(commandPrefixWithoutQuoteReply2)) {
            expr = messageString.substring(commandPrefixWithoutQuoteReply1.length()).trim();
            hasQuoteReply = false;
        } else if (messageString.startsWith(commandPrefixWithQuoteReply) && messageString.charAt(1) != '<') {
            hasQuoteReply = true;
            expr = messageString.substring(commandPrefixWithQuoteReply.length()).trim();
        } else {
            return false;
        }

        if (!Evaluator.enable) {
            if (Evaluator.hintDisable) {
                event.getSubject().sendMessage(errorPrefixMessage + "已禁用程序执行");
            }
            return true;
        }

        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        if (hasQuoteReply) {
            messageChainBuilder.add(new QuoteReply(event.getMessage()));
        }

        Group group = null;
        if (event instanceof GroupMessageEvent) {
            group = ((GroupMessageEvent) event).getGroup();
        }

        String envId;
        Matcher useEnvPatternMatcher = useEnvPattern.matcher(expr);
        if (useEnvPatternMatcher.find()) {
            envId = useEnvPatternMatcher.group(1);
            if ("my".equals(envId)) {
                envId = String.valueOf(event.getSender().getId());
            }
            expr = expr.substring(useEnvPatternMatcher.end() + 1).trim();
        } else {
            envId = EnvManager.GLOBAL_ENV_ID;
        }

        Optional<EnvInfo> optEnv = envManager.getEnv(envId);
        if (optEnv.isPresent()) {
            EnvInfo env = optEnv.get();
            boolean canUseEnv = true;
            switch (env.scope) {
                case Group:
                    canUseEnv = group != null && group.getId() == env.ownerId;
                    break;
                case Private:
                    canUseEnv = event.getSender().getId() == env.ownerId;
                    break;
                default:
                    break;
            }
            if (canUseEnv) {
                APIResult evalResult = evaluator.eval(expr, env.id, event.getSender(), group);
                if (!evalResult.isOk()) {
                    String text = EvalExnResultHandler.toReadableText(evalResult, expr);
                    if (StringUtils.isNotEmpty(text)) {
                        messageChainBuilder.add(errorPrefixMessage + text);
                    }
                } else {
                    boolean hasOutput = OutputHandler.handle(messageChainBuilder, evalResult.output, event);
                    if (StringUtils.isNotEmpty(evalResult.value)) {
                        if (!hasOutput || !"#<void>".equals(evalResult.value)) {
                            if (hasOutput) {
                                // output后面加换行
                                messageChainBuilder.add("\n");
                            }
                            messageChainBuilder.add(StringUtils.trim(evalResult.value));
                        }
                    }
                }
            } else {
                messageChainBuilder.add(errorPrefixMessage + "无权限使用环境 " + envId);
            }
        } else {
            messageChainBuilder.add(errorPrefixMessage + "不存在的环境 " + envId);
        }

        event.getSubject().sendMessage(messageChainBuilder.build());
        return true;
    }
}