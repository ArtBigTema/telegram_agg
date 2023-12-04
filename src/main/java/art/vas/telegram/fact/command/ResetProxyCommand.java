package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.SimpleMessageCommando;
import art.vas.telegram.fact.service.ProxyService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class ResetProxyCommand implements SimpleMessageCommando {
    private final ProxyService proxyService;

    @Override
    public String getCommandLine() {
        return "/reset";
    }

    @Override
    public SendMessage answer(Message message) {
        String text = message.getText();
        String[] strings = StringUtils.splitPreserveAllTokens(text, " \n");
        proxyService.after(Arrays.asList(ArrayUtils.subarray(strings, 1, strings.length)));
        return new SendMessage(message.getChatId().toString(), "Успешно, наверно...");
    }
}
