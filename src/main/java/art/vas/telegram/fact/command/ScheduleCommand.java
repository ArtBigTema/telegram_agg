package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.CallBackMessageCommando;
import art.vas.telegram.fact.model.Users;
import art.vas.telegram.fact.model.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public abstract class ScheduleCommand implements CallBackMessageCommando<SendMessage> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm");
    private final UserRepo userRepo;
    private final BiFunction<Users, String, Users> customizer;

    @Override
    public String getCommandLine() {
        return "/schedule";
    }

    @Override
    public String getCallBack() {
        return "Во сколько хотите получить уведомление о " + getSubject() + "?";
    }

    @Override
    public SendMessage alternative(CallbackQuery message) {
        Long chatId = message.getMessage().getChatId();
        Optional<Users> user = userRepo.findById(chatId);
        String time = message.getData() + ":00";
        userRepo.save(customizer.apply(user.orElseThrow(), time));
        String text = "Успешно. Уведомлю вас о " + getSubject() + " в " + time;
        return new SendMessage(chatId.toString(), text);
    }

    protected abstract String getSubject();

    @Override
    public SendMessage answer(Message message) {
        SendMessage sendMessage = new SendMessage(message.getChatId().toString(), getCallBack());

        List<InlineKeyboardButton> list = IntStream.rangeClosed(5, 10)
                .mapToObj(i -> LocalTime.of(i, 0)).map(t -> {
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                    String format = t.format(formatter);
                    inlineKeyboardButton.setText(format);
                    inlineKeyboardButton.setCallbackData(format);
                    return inlineKeyboardButton;
                }).toList();

        sendMessage.setReplyMarkup(new InlineKeyboardMarkup(Collections.singletonList(list)));

        return sendMessage;
    }
}
