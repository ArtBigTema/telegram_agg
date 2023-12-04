package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.SimpleMessageCommando;
import art.vas.telegram.fact.model.Fact;
import art.vas.telegram.fact.model.repo.FactRepo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetFactCommand implements SimpleMessageCommando {
    private final FactRepo factRepo;

    @Override
    public String getAnswer() {
        //        long count = factRepo.count() - 1; // i % count + 1
        long l = RandomUtils.nextLong(1, factRepo.count());
        return factRepo.findById(l).map(Fact::getFact).orElseThrow();
    }

    @Override
    public List<String> getCommandLines() {
        return Arrays.asList("/fact", "/факт");
    }

    @Override
    public SendMessage answer(Message message) {
        return new SendMessage(message.getChatId().toString(), getAnswer());
    }
}
