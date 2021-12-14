package bot.message_handlers;

import bot.AdminManager;
import bot.block.BlockUser;
import bot.block.BlockUserManager;
import bot.BotEmoji;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EvalLispHandler {
    private static Pattern usePrefixWithQuoteReplyPattern = Pattern.compile("^(\\s*#\\s+)\\S.*");
    private static Pattern usePrefixWithoutQuoteReplyPattern = Pattern.compile("^(\\s*[!！]\\s*)\\S.*");
    private static Pattern useEnvPattern = Pattern.compile("\\s*#env\\s+([\\w-]+)");

    private Evaluator evaluator;

    private EnvManager envManager;

    public EvalLispHandler(APIAccess apiAccess) {
        evaluator = new Evaluator(apiAccess);
        envManager = new EnvManager(apiAccess);
    }

    public boolean testAndHandle(String messageString, MessageEvent event) {
        String expr;
        boolean hasQuoteReply;

        Matcher matcher = usePrefixWithoutQuoteReplyPattern.matcher(messageString);
        if (matcher.matches()) {
            expr = messageString.substring(matcher.group(1).length()).trim();
            hasQuoteReply = false;
        } else {
            matcher = usePrefixWithQuoteReplyPattern.matcher(messageString);
            if (matcher.matches()) {
                expr = messageString.substring(matcher.group(1).length()).trim();
                hasQuoteReply = true;
            } else {
                if (LispCodeChecker.maybeHasLispCode(messageString)) {
                    expr = messageString;
                    hasQuoteReply = false;
                } else {
                    return false;
                }
            }
        }

        if (!Evaluator.enable) {
            if (Evaluator.hintDisable) {
                event.getSubject().sendMessage(BotEmoji.evalError + "已禁用程序执行");
            }
            return true;
        }

        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        if (hasQuoteReply) {
            messageChainBuilder.add(new QuoteReply(event.getMessage()));
        }

        if (BlockUserManager.checkNowIsBlock(event.getSender().getId())) {
            return true;
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
                    String text = EvalExnResultHandler.toReadableText(evalResult, expr, event);
                    if (StringUtils.isNotEmpty(text)) {
                        messageChainBuilder.add(BotEmoji.evalError + text);
                    }

                    if (EvalExnResultHandler.isOutOfMemory(evalResult)) {
                        if (!AdminManager.isAdmin(event.getSender())) {
                            BlockUser blockUser = new BlockUser(event.getSender());
                            blockUser.startTime = LocalDateTime.now();
                            blockUser.minutes = 120;
                            BlockUserManager.add(blockUser);
                            BlockUserManager.addMessage(messageChainBuilder, blockUser);
                        }
                    }
                } else {
                    boolean isBigString = OutputHandler.isBigString(evalResult.output);
                    if (!isBigString) {
                        boolean hasOutput = OutputHandler.handle(messageChainBuilder, evalResult.output, event);
                        if (StringUtils.isNotEmpty(evalResult.value)) {
                            if (!hasOutput || !"#<void>".equals(evalResult.value)) {
                                if (hasOutput) {
                                    // output后面加换行
                                    messageChainBuilder.add("\n");
                                }
                                isBigString = OutputHandler.isBigString(evalResult.value);
                                if (!isBigString) {
                                    messageChainBuilder.add(StringUtils.trim(evalResult.value));
                                }
                            }
                        }
                    }

                    if (isBigString) {
                        messageChainBuilder.add(BotEmoji.evalError + "程序输出内容超过长度限制");
                        if (!AdminManager.isAdmin(event.getSender())) {
                            BlockUser blockUser = new BlockUser(event.getSender());
                            blockUser.startTime = LocalDateTime.now();
                            blockUser.minutes = 30;
                            BlockUserManager.add(blockUser);
                            BlockUserManager.addMessage(messageChainBuilder, blockUser);
                        }
                    }
                }
            } else {
                messageChainBuilder.add(BotEmoji.evalError + "无权限使用环境 " + envId);
            }
        } else {
            messageChainBuilder.add(BotEmoji.evalError + "不存在的环境 " + envId);
        }

        event.getSubject().sendMessage(messageChainBuilder.build());
        return true;
    }
}