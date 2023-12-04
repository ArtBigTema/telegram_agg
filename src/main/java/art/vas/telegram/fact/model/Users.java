package art.vas.telegram.fact.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalTime;
import java.util.Optional;

@Data
@Entity
@Accessors(chain = true)
public class Users {
    @Id
    Long id;

    @NotBlank
    String author;
    @NotBlank
    String chatId;
    LocalTime cron;
    LocalTime cronWeather;
    LocalTime cronToday;

    public static Users of(String login, Long chat) {
        Users msg = new Users();
        msg.setChatId(chat.toString());
        msg.setAuthor(login);
        msg.setId(chat);
        return msg;
    }


    public static LocalTime getTimeIfCorrect(String text) {
        return Optional.ofNullable(text).map(LocalTime::parse).orElse(null);
    }
}
