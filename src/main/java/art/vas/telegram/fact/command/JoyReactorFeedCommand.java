package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.Commando;
import art.vas.telegram.fact.config.JsonConfig;
import art.vas.telegram.fact.utils.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoyReactorFeedCommand implements Commando<SendMediaGroup> {
    public static final String url = "https://joyreactor.cc/";
    @Getter
    public final Map<Long, String> nextMsg = new HashMap<>();
    private final Map<Long, Integer> latestMsg = new HashMap<>();

    public static List<byte[]> getImage(Element element, String path) {
        Elements elements = element.select(path);
        LinkedList<byte[]> list = new LinkedList<>();
        for (Element el : elements) {
            list.add(Utils.safetyGet(() -> getImage(el)));
        }
        return list;
    }

    public static byte[] getImage(Element element) {//element.select("div[class=image]")
        String src = element.select("img").first().attr("src");
        if (StringUtils.containsAny(src, "comment", "gif")) return null;
        src = StringUtils.prependIfMissing(src, "https:");

        byte[] bytes = Objects.requireNonNull(JsonConfig.restTemplate.getForObject(src, byte[].class));
        if (bytes.length > 1_000_000) return null;
        return bytes;
    }

    @Override
    public String getCommandLine() {
        return "/enjoyreactor";
    }

    @Override
    @SneakyThrows
    public SendMediaGroup answer(TelegramLongPollingBot bot, Message message) {
        Long chatId = message.getChatId();
        bot.executeAsync(new DeleteMessage(chatId.toString(), message.getMessageId()));
        Integer remove = latestMsg.remove(chatId);
        if (Objects.nonNull(remove)) {
            bot.executeAsync(new DeleteMessage(chatId.toString(), remove));
        }
        String next = nextMsg.getOrDefault(chatId, StringUtils.EMPTY);

        String text = message.getText();
        if (StringUtils.isNotBlank(text)) {
            next = "search?q=" + UriUtils.encode(text, UTF_8);
        }

        return getSendMediaGroup(chatId, next);
    }

    @SneakyThrows
    public SendMediaGroup getSendMediaGroup(Long chatId, String next) {
        Document doc = Jsoup.connect(url + next.trim()).get();

        log.info("Url: " + doc.location());
        Elements select = doc.select("div[class=postContainer]");

        List<InputMedia> list = new LinkedList<>();

        List<byte[]> images = select.stream()
                .flatMap(element -> Utils.sublist(getImage(element, "div[class=image]"), 3)
                        .stream()).filter(ArrayUtils::isNotEmpty).toList();

        for (byte[] image : images) {
            String random = RandomStringUtils.random(5, true, false);
            InputMediaPhoto photo = new InputMediaPhoto();
            photo.setNewMediaStream(new ByteArrayInputStream(image));
            photo.setMedia("attach://" + random);
            photo.setNewMedia(true);
            photo.setMediaName(random);
            list.add(photo);
        }

        list = Utils.sublist(list, 10);
        next = doc.select("a[class=next]").attr("href");
        nextMsg.put(chatId, next.substring(1));
        list.iterator().next().setCaption(getCommandLine());
        return new SendMediaGroup(chatId.toString(), list);
    }
}
