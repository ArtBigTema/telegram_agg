package art.vas.telegram.fact.command.common;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.io.Serializable;

public interface CallBackMessageCommando<T extends PartialBotApiMethod<? extends Serializable>> extends SimpleMessageCommando {
    String getCallBack();

    T alternative(CallbackQuery message);

    default void alternative(CallbackQuery message, TelegramLongPollingBot bot) {
        T alternative = alternative(message);
        execute(alternative, bot);
    }
}
