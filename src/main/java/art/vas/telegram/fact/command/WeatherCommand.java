package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.SimpleMessageCommando;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.guava.Preconditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static art.vas.telegram.fact.config.JsonConfig.restTemplate;


@Component
public class WeatherCommand implements SimpleMessageCommando {
    private static final String yandex = "https://api.weather.yandex.ru/v2/forecast?extra=true&";
    private static final String copyright = "\n\nПо данным сервиса Яндекс Погода";

    private final static Map<LocalDate, JsonNode> TODAY = new HashMap<>();

    @Value("${ya.weather.token}")
    String yandexToken;

    @Value("${samara}")
    String samara;

    @Override
    public String getCommandLine() {
        return "/погода";
    }

    public List<String> keys() {
        JsonNode jsonNode = TODAY.get(LocalDate.now());
        String s = jsonNode.get("fact").get("condition").asText();
        s = StringUtils.remove(StringUtils.remove(s, "light-"), "with-");
        String[] split = StringUtils.split(StringUtils.defaultString(s), "-");
        List<String> list = new LinkedList<>(Arrays.asList(split));
        list.add(jsonNode.get("fact").get("season").asText());
        list.add("landscape");
        return list;
    }

    @Override
    public SendMessage answer(String chatId) {
        JsonNode jsonNode = TODAY.get(LocalDate.now());

        if (Objects.isNull(jsonNode)) {
            String url = yandex.concat(samara);
            HttpEntity<?> entity = new HttpEntity<>(CollectionUtils.toMultiValueMap(Collections
                    .singletonMap("X-Yandex-API-Key", Collections.singletonList(yandexToken))));
            ResponseEntity<JsonNode> forEntity = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            Preconditions.checkArgument(forEntity.getStatusCode().is2xxSuccessful());
            TODAY.put(LocalDate.now(), jsonNode = forEntity.getBody());
        }

        StringBuilder sb = new StringBuilder();
        int yesterday = jsonNode.get("yesterday").get("temp").asInt();
        sb.append("Вчера было: ").append(yesterday).append(" градусов ");
        JsonNode fact = jsonNode.get("fact");
        int today = fact.get("temp").asInt();

        sb.append(yesterday == today ? "и" : ", а").append(" сегодня: ").append(today).append(".\n");
        sb.append("Но чувствуется как: ").append(fact.get("feels_like").asInt()).append(". ");
        sb.append("Ветер ").append(fact.get("wind_speed").asInt()).append("мс.\n");

        JsonNode tom = jsonNode.get("forecasts").get(0);
        sb.append("Ну а завтра рассвет в ").append(tom.get("sunrise").asText());
        sb.append(", закат в ").append(tom.get("sunset").asText()).append(".\n");

        tom = tom.get("parts").get("day_short");
        sb.append("Скорость ветра ").append(tom.get("wind_speed")).append("мс.\n");
        sb.append("Температура за окном ").append(tom.get("temp"));
        sb.append(", но ощущается как ").append(tom.get("feels_like")).append(".");

        sb.append(copyright);

        return new SendMessage(chatId, sb.toString());
    }
}
