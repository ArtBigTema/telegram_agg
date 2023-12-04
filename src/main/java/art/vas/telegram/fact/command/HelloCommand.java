package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.SimpleMessageCommando;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class HelloCommand implements SimpleMessageCommando {
    @Override
    public String getAnswer() {
        return "Приветствую тебя, путник. Жмакни /fact";
    }

    @Override
    public String getCommandLine() {
        return "/start";
    }

    @Override
    public SendMessage answer(Message message) {
        return new SendMessage(message.getChatId().toString(), getAnswer());
    }
}
