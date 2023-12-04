package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.SimpleMessageCommando;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;

@Component
public class HelloAboutMeCommand implements SimpleMessageCommando {
    @Override
    public String getAnswer() {
        return "Приветствую тебя, путник. Вот что я умею:\n" +
                """
                         /fact или факт - рандомный факт о чём угодно
                         /photo или фото - рандомное фото со стока
                         /today или праздник - какой сегодня праздник
                         /weather или погода - погода на сегодня и завтра
                         /joke или шутка или анекдот - рандомный анекдот
                         /synonym или синоним и слово - синоним к слову
                         /kandinsky или Кандинский и инфо о картинке
                         /dali или Дали и инфо о картинке
                         /joyrandom рандомная картинка с сайта joyreactor
                         А также вы можете просто писать любой текст, просьбу или идею
                         Например: нарисуй кота, придумай сказку, рецепт сырного супа
                        """; //                /enjoyreactor поток с сайта joyreactor
    }

    @Override
    public List<String> getCommandLines() {
        return Arrays.asList("/start", "/about", "/привет", "/старт");
    }

    @Override
    public SendMessage answer(Message message) {
        return new SendMessage(message.getChatId().toString(), getAnswer());
    }
}
