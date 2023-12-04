package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.Commando;
import art.vas.telegram.fact.service.ProxyService;
import art.vas.telegram.fact.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static art.vas.telegram.fact.config.JsonConfig.restTemplate;
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

    @Value("${fusion.proxy.url}")
    String fusionUrl;
    @Value("${fusion.proxy.token}")
    String fusionToken;
    @Value("${fusion.proxy.secret}")
    String fusionSecret;
    private static int id = 4;

    // @PostConstruct
    public void post() {
        HttpEntity<?> entity = new HttpEntity<>(CollectionUtils.toMultiValueMap(
                Map.of("X-Secret", Collections.singletonList(fusionSecret),
                        "X-Key", Collections.singletonList(fusionToken))));

        ResponseEntity<ArrayNode> response = restTemplate.exchange(
                fusionUrl + "models", HttpMethod.GET, entity, ArrayNode.class);
//        Preconditions.checkArgument(response.getStatusCode().is2xxSuccessful());
        id = Optional.ofNullable(Utils.safetyGet(() -> response
                .getBody().iterator().next().get("id").asInt())).orElse(id);
        // models
    }

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

        String url = forEntity.getBody().findValue("url").asText();
//        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET,
//                new HttpEntity<>(null, toMultiValueMap(Map.of(
//                        HOST, singletonList(URI.create(url).getHost())))),
//                byte[].class);
        byte[] bytes = Utils.safetyGet(() -> Jsoup.connect(url) // todo restTemplate
                .ignoreContentType(true).execute().bodyAsBytes());

        return new SendPhoto(chatId, new InputFile(new ByteArrayInputStream(requireNonNull(bytes)), "random"));
    }
}
