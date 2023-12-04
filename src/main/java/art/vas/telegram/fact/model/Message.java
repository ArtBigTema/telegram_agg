package art.vas.telegram.fact.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.Update;

import static jakarta.persistence.GenerationType.IDENTITY;

@Data
@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    Long id;

    @NotBlank
    String author;
    @NotNull
    String message;

    @SneakyThrows
    public static Message of(Update update, String login, String text) {
        Message msg = new Message();
        msg.setMessage(text);
        msg.setAuthor(login);
        return msg;
    }
}
