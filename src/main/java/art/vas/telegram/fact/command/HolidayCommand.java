package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.SimpleMessageCommando;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Component
public class HolidayCommand implements SimpleMessageCommando {
    private static final String holiday = "https://kakoysegodnyaprazdnik.ru/";

    private final static Map<LocalDate, List<String>> TODAY = new HashMap<>();

    @Override
    public String getCommandLine() {
        return "/праздник";
    }

    public List<String> getCommandLines() {
        return Arrays.asList(getCommandLine(), "/today", "holiday");
    }

    @Override
    @SneakyThrows
    public SendMessage answer(String chatId) {
        List<String> text = TODAY.get(LocalDate.now());

        if (Objects.isNull(text)) {
            Document doc = Jsoup.connect(holiday).get();
            Elements elements = doc.select("span[itemprop=text]");
            TODAY.put(LocalDate.now(), text = elements.stream().map(Element::text).limit(5).toList());
        }

        String join = String.join(",\n", text);
        join += "\n\n Хотите подписаться? Нажмите /scheduleToday";

        return new SendMessage(chatId, join);
    }
}
