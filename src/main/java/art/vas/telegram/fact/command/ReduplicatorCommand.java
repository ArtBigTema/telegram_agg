package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.CallBackMessageCommando;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.guava.Preconditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static art.vas.telegram.fact.AllController.regexp;
import static art.vas.telegram.fact.config.JsonConfig.restTemplate;

@Component
@RequiredArgsConstructor
public class ReduplicatorCommand implements CallBackMessageCommando<SendMessage> {
    private static final List<Character> volume = Arrays.asList('а', 'е', 'ё', 'и', 'о', 'у', 'ы', 'э', 'ю', 'я');
    private static final String yandex = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key=%s&lang=ru-ru&text=";

    // todo divide
    private final ObjectMapper objectMapper;
    private final ChatGptCommando chatGptCommando;

    private static final Map<Character, Character> map = new HashMap<>() {
        {
            put('а', 'я');
            put('о', 'ё');
            put('у', 'ю');
            put('э', 'е');
            put('ы', 'и');
        }
    };

    @Value("${ya.dictionary.token}")
    String yandexToken;

    @Override
    public String getCallBack() {
        return "Похоже вы материтесь, хотите альтернативу?";
    }

    public String reduplicate(Message message) {
        String text = message.getText();

        StringBuilder b = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (b.isEmpty() && !volume.contains(c)) continue;
            b.append(map.getOrDefault(c, c));
        }

        return b.insert(0, "ху").toString();
    }

    @Override
    public SendMessage answer(TelegramLongPollingBot bot, Message message) {
        String text = message.getText();
        if (text.charAt(0) == '!') {
            return new SendMessage(message.getChatId().toString(), sinonym(text.substring(1)));
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
        return new SendMessage(message.getMessage().getChatId().toString(), sinonym(message.getData()));
    }

    @SneakyThrows
    private String sinonym(String text) {//https://how-to-all.com/%D1%81%D0%B8%D0%BD%D0%BE%D0%BD%D0%B8%D0%BC%D1%8B:%D0%BF%D1%80%D0%B8%D0%BD%D1%86
//        URL url = ResourceUtils.toURL("https://how-to-all.com/".concat("синонимы:").concat(text));
//        Document doc = Jsoup.connect(url.toString()).get();
        String url = String.format(yandex, yandexToken).concat(text);
        ResponseEntity<JsonNode> forEntity = restTemplate.getForEntity(url, JsonNode.class);
        Preconditions.checkArgument(forEntity.getStatusCode().is2xxSuccessful());

        List<String> all = forEntity.getBody().findValuesAsText("text");
        return "Синонимы: " + String.join(", ", all);
    }
}
