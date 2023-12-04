package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.CallBackMessageCommando;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JokeByCallBackCommand implements CallBackMessageCommando<SendMessage> {
    private final JokeCommand jokeCommand;

    @Override
    public String getAnswer() {
        return jokeCommand.getAnswer();
    }

    @Override
    public List<String> getCommandLines() {
        return List.of("/joker");
    }

    @Override
    public String getCallBack() {
        return "Хотите больше юмора?";
    }

    @Override
    public SendMessage alternative(CallbackQuery message) {
        Long chatId = message.getMessage().getChatId();
        return new SendMessage(chatId.toString(), jokeCommand
                .getAnswer(message.getData(), false));
    }

    @Override
    public SendMessage answer(Message message) {
        SendMessage sendMessage = new SendMessage(message.getChatId().toString(), getCallBack());

        List<List<Joke>> partition = ListUtils.partition(Arrays.asList(Joke.values()), 3);

        List<List<InlineKeyboardButton>> list = partition.stream()
                .map(t ->
                        t.stream().map(s -> {
                            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                            inlineKeyboardButton.setCallbackData(s.name().substring(1));
                            inlineKeyboardButton.setText(s.title);
                            return inlineKeyboardButton;
                        }).collect(Collectors.toList())
                ).toList();

        sendMessage.setReplyMarkup(new InlineKeyboardMarkup(list));

        return sendMessage;
    }

    @Getter
    @AllArgsConstructor
    enum Joke {
        _1("Анекдот"),
        //        _2("Рассказы"),
        _3("Стишки"),
        _4("Афоризмы"),
        _5("Цитаты"),
        _6("Тосты"),
        _8("Статусы"),
        //        _12("Рассказы 18+"),
        _11("Анекдот 18+"),
        _13("Стишки 18+"),
        _14("Афоризмы 18+"),
        _15("Цитаты 18+"),
        _16("Тосты 18+"),
        _18("Статусы 18+");
        private final String title;
    }
}
