package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.Commando;
import art.vas.telegram.fact.service.ProxyService;
import art.vas.telegram.fact.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.guava.Preconditions;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.CollectionUtils.toMultiValueMap;


@Component
@RequiredArgsConstructor
public class DalleImageCommand implements Commando<SendPhoto> {
    public static final String answer = "{\"prompt\": \"%s\",\"n\": 1,\"size\": \"512x512\"}";
    final RestTemplate proxyRestTemplate;
    @Value("${open.ai.url}")
    String openAiUrl;
    @Value("${panda.proxy.url}")
    String pandaAiUrl;

    @Value("${open.ai.token}")
    String openAiToken;

    @Override
    public String getCommandLine() {
        return "/дали";
    }

    @Override
    public SendPhoto answer(Message message) {
        String chatId = message.getChatId().toString();
        String text = StringUtils.removeStart(message.getText(), getCommandLine());
        String format = String.format(answer, text);
//        JsonNode body = objectMapper.valueToTree(format);

        HttpEntity<?> entity = new HttpEntity<>(format,
                toMultiValueMap(Map.of(AUTHORIZATION, singletonList(openAiToken),
                        CONTENT_TYPE, singletonList(MediaType.APPLICATION_JSON))));
        ResponseEntity<JsonNode> forEntity = ProxyService.repeat(() -> proxyRestTemplate
                .exchange(pandaAiUrl + "images/generations", HttpMethod.POST, entity, JsonNode.class));
        Preconditions.checkState(forEntity.getStatusCode().is2xxSuccessful());

        String url = forEntity.getBody().findValue("url").asText();
        byte[] bytes = Utils.safetyGet(() -> Jsoup.connect(url) // todo restTemplate
                .ignoreContentType(true).execute().bodyAsBytes());

        return new SendPhoto(chatId, new InputFile(new ByteArrayInputStream(requireNonNull(bytes)), "random"));
    }
}
