package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.SimpleMessageCommando;
import art.vas.telegram.fact.config.JsonConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JokeCommand implements SimpleMessageCommando {
    private static final String rzhu = "http://rzhunemogu.ru/RandJSON.aspx?CType=";

    @Override
    public String getAnswer() {
        return getAnswer("11", true);
    }

    public String getAnswer(String i, boolean withSuggest) {
        String node = JsonConfig.restTemplate.getForObject(rzhu + i, String.class);
        node = StringUtils.substringBetween(node, "{\"content\":\"", "\"}");

        if (StringUtils.contains(node, "Support@RzhuNeMogu.ru")) {
            node = "Ошибка обращения к стороннему сервису. Попробуйте позднее";
        } else if (withSuggest) {
            node += "\n\n Хотите больше такого юмора? Нажмите /joker";
        }
        return node;
    }

    @Override
    public List<String> getCommandLines() {
        return Arrays.asList("/joke", "/шутка", "/анекдот");
    }

    @Override
    public SendMessage answer(Message message) {
        return new SendMessage(message.getChatId().toString(), getAnswer());
    }
}
