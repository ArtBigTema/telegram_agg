package art.vas.telegram.fact.command.common;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface SimpleMessageCommando extends Commando<SendMessage> {
    default String getAnswer() {
        return null;
    }
}
