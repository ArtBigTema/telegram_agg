package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.SimpleMessageCommando;
import art.vas.telegram.fact.service.ProxyService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.CollectionUtils.toMultiValueMap;

@Component
@RequiredArgsConstructor
public class ChatGptCommando implements SimpleMessageCommando {
    public static final String answer = "{\"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}]}";
    final RestTemplate proxyRestTemplate;
    @Value("${open.ai.url}")
    String openAiUrl;
    @Value("${panda.proxy.url}")
    String pandaAiUrl;

    @Value("${open.ai.token}")
    String openAiToken;

    @Override
    public List<String> getCommandLines() {
        return Arrays.asList("/bot", "/robot", "/бот", "/gpt");
    }

    @Override
    public SendMessage answer(TelegramLongPollingBot bot, Message message) {//        ActionType.TYPING
        String chatId = message.getChatId().toString();
        SendChatAction method = new SendChatAction();
        method.setChatId(chatId);
        method.setAction(ActionType.TYPING);
        execute(method, bot);

        String format = String.format(answer, message.getText());
//        JsonNode body = objectMapper.valueToTree(format);

        HttpEntity<?> entity = new HttpEntity<>(format,
                toMultiValueMap(Map.of(AUTHORIZATION, singletonList(openAiToken),
                        CONTENT_TYPE, singletonList(MediaType.APPLICATION_JSON))));
        ResponseEntity<JsonNode> forEntity = ProxyService.repeat(() -> proxyRestTemplate
                .exchange(pandaAiUrl+"chat/completions", HttpMethod.POST, entity, JsonNode.class));

        return new SendMessage(chatId, forEntity.getBody().findValue("content").asText());
    }
}
