package art.vas.telegram.fact.command.common;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public interface Commando<T extends PartialBotApiMethod<? extends Serializable>> {
    default String getCommandLine() {
        return StringUtils.EMPTY;
    }

    default List<String> getCommandLines() {
        return Collections.singletonList(getCommandLine());
    }

    default T answer(TelegramLongPollingBot bot, Message message) {
        return answer(message);
    }

    default T answer(Message message) {
        return answer(message.getChatId().toString());
    }

    default T answer(String chatId) {
        return null;
    }

    default void takeIt(Update update, TelegramLongPollingBot bot) {
        T answer = answer(bot, update.getMessage());
        execute(answer, bot);
    }

    default List<Message> execute(PartialBotApiMethod<? extends Serializable> t, TelegramLongPollingBot bot) {
        if (Objects.isNull(t)) return Collections.emptyList();
        try {
            switch (t) {
                case SendMessage s -> {
                    return Collections.singletonList(bot.execute(s));
                }
                case SendPhoto l -> {
                    return Collections.singletonList(bot.execute(l));
                }
                case SendAnimation a -> {
                    return Collections.singletonList(bot.execute(a));
                }
                case SendMediaGroup d -> {
                    return bot.execute(d);
                }
                case SendChatAction a -> {
                    bot.execute(a);
                    return Collections.emptyList();
                }
                default -> System.err.println("err");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("smth w " + e.getMessage());
        }
        return Collections.emptyList();
    }
}
