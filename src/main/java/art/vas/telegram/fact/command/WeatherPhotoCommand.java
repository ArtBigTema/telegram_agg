package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.Commando;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

import java.util.List;

@Component
@AllArgsConstructor
public class WeatherPhotoCommand implements Commando<SendPhoto> {
    WeatherCommand weatherCommand;
    PhotoCommand photoCommand;

    @Override
    public String getCommandLine() {
        return "/weather";
    }

    @Override
    public SendPhoto answer(String chatId) {
        SendMessage w = weatherCommand.answer(chatId);
        List<String> keys = weatherCommand.keys();
        SendPhoto photo = photoCommand.answer(chatId, keys);
        photo.setCaption(w.getText());
        return photo;
    }
}
