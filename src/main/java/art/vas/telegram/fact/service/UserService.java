package art.vas.telegram.fact.service;

import art.vas.telegram.fact.model.Message;
import art.vas.telegram.fact.model.Users;
import art.vas.telegram.fact.model.repo.MsgRepo;
import art.vas.telegram.fact.model.repo.UserRepo;
import art.vas.telegram.fact.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserService {
    private final MsgRepo msgRepo;
    private final UserRepo userRepo;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Transactional
    public void log(Update update) {
        String login = Objects.requireNonNull(Utils.safetyGet(
                () -> update.getCallbackQuery().getFrom().getUserName(),
                () -> update.getMessage().getFrom().getUserName()));
        Long chatId = Objects.requireNonNull(Utils.safetyGet(
                () -> update.getCallbackQuery().getMessage().getChatId(),
                () -> update.getMessage().getChatId()));

        msgRepo.save(Message.of(update, login, objectMapper
                .writerWithDefaultPrettyPrinter().writeValueAsString(update)));

        if (!userRepo.existsById(chatId)) {
            userRepo.save(Users.of(login, chatId));
        }
    }
}
