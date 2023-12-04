package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.CallBackMessageCommando;
import com.google.common.collect.ImmutableBiMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;
import java.util.Map;

import static art.vas.telegram.fact.AllController.regexp;

@Component
@RequiredArgsConstructor
public class ReduplicatorCommand implements CallBackMessageCommando<SendMessage> {
    private final SynonymCommand synonymCommando;
    private final ChatGptCommando chatGptCommando;

    private static final Map<Character, Character> map = ImmutableBiMap.of(
            'а', 'я', 'о', 'ё', 'у', 'ю', 'э', 'е', 'ы', 'и');

    @Override
    public String getCallBack() {
        return "Похоже вы материтесь, хотите альтернативу?";
    }

    public String reduplicate(Message message) {
        String text = message.getText();

        StringBuilder b = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (b.isEmpty() && !map.containsKey(c) && !map.containsValue(c)) continue;
            b.append(map.getOrDefault(c, c));
        }

        return b.insert(0, "ху").toString();
    }

    @Override
    public SendMessage answer(TelegramLongPollingBot bot, Message message) {
        String text = message.getText();
        if (text.charAt(0) == '!') {
            return new SendMessage(message.getChatId().toString(), synonymCommando.sinonym(text.substring(1)));
        }
        if (StringUtils.countMatches(text, ' ') > 0) {
            return chatGptCommando.answer(bot, message);
        }

        if (regexp.matcher(text).matches()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText("Да");
            inlineKeyboardButton.setCallbackData(text);
            SendMessage sendMessage = new SendMessage(message.getChatId().toString(), getCallBack());
            sendMessage.setReplyMarkup(new InlineKeyboardMarkup(Collections.singletonList(Collections.singletonList(inlineKeyboardButton))));
            return sendMessage;
        }

        return new SendMessage(message.getChatId().toString(), reduplicate(message));
    }

    @Override
    public SendMessage alternative(CallbackQuery message) {
        return new SendMessage(message.getMessage().getChatId().toString(), synonymCommando.sinonym(message.getData()));
    }
}
