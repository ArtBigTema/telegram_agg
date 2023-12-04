package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.SimpleMessageCommando;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.guava.Preconditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;

import static art.vas.telegram.fact.config.JsonConfig.restTemplate;

@Component
public class SynonymCommand implements SimpleMessageCommando {
    private static final String yandex = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key=%s&lang=ru-ru&text=";

    @Value("${ya.dictionary.token}")
    String yandexToken;

    @Override
    public List<String> getCommandLines() {
        return Arrays.asList("/synonym", "/синоним");
    }

    @Override
    public SendMessage answer(TelegramLongPollingBot bot, Message message) {
        String chatId = message.getChatId().toString();
        if (StringUtils.isBlank(message.getText())) {
            execute(new SendMessage(chatId, "Необходимо само слово после команды"), bot);
            return null;
        }
        return new SendMessage(chatId, sinonym(message.getText()));
    }

    public String sinonym(String text) {//https://how-to-all.com/%D1%81%D0%B8%D0%BD%D0%BE%D0%BD%D0%B8%D0%BC%D1%8B:%D0%BF%D1%80%D0%B8%D0%BD%D1%86
//        URL url = ResourceUtils.toURL("https://how-to-all.com/".concat("синонимы:").concat(text));
//        Document doc = Jsoup.connect(url.toString()).get();
        String url = String.format(yandex, yandexToken).concat(text);
        ResponseEntity<JsonNode> forEntity = restTemplate.getForEntity(url, JsonNode.class);
        Preconditions.checkArgument(forEntity.getStatusCode().is2xxSuccessful());

        List<String> all = forEntity.getBody().findValuesAsText("text");
        return "Синонимы: " + String.join(", ", all);
    }
}
