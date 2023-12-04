package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.Commando;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoyReactorRandomCommand implements Commando<SendPhoto> {
    private final Map<Long, Integer> latestMsg = new HashMap<>();

    @Override
    public String getCommandLine() {
        return "/joyrandom";
    }

    @Override
    @SneakyThrows
    public SendPhoto answer(TelegramLongPollingBot bot, Message message) {
        Long chatId = message.getChatId();
        bot.executeAsync(new DeleteMessage(chatId.toString(), message.getMessageId()));
        Integer remove = latestMsg.remove(chatId);
        if (nonNull(remove)) {
            bot.executeAsync(new DeleteMessage(chatId.toString(), remove));
        }

        Document doc = Jsoup.connect(JoyReactorFeedCommand.url + "/random").get();
        log.info("Url: " + doc.location());
        byte[] image = requireNonNull(JoyReactorFeedCommand.getImage(
                requireNonNull(doc.select("div[class=image]").first())));

        SendPhoto sendPhoto = new SendPhoto(chatId.toString(), new InputFile(
                new ByteArrayInputStream(image), message.getMessageId().toString()));
        sendPhoto.setCaption(getCommandLine());
        return sendPhoto;
    }

    @Override
    public List<Message> execute(PartialBotApiMethod<? extends Serializable> t, TelegramLongPollingBot bot) {
        List<Message> execute = Commando.super.execute(t, bot);
        Message message = execute.iterator().next();
        latestMsg.put(message.getChatId(), message.getMessageId());
        return execute;
    }
}