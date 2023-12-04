package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.Commando;
import art.vas.telegram.fact.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.guava.Preconditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static art.vas.telegram.fact.config.JsonConfig.restTemplate;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpHeaders.HOST;
import static org.springframework.util.MimeTypeUtils.generateMultipartBoundaryString;


@Component
public class KandinskyImageCommand implements Commando<SendPhoto> {
    public static final String answer = "{\"type\":\"GENERATE\",\"generateParams\":{ \"query\": \"%s\"}}";

    @Value("${fusion.proxy.url}")
    String fusionUrl;
    private final MultiValueMap<String, String> headers;
    private static int id = 4;

    public KandinskyImageCommand(@Value("${fusion.proxy.token}") String fusionToken,
                                 @Value("${fusion.proxy.secret}") String fusionSecret) {
        headers = CollectionUtils.toMultiValueMap(
                Map.of("X-Secret", singletonList(fusionSecret),
                        HOST, singletonList("api-key.fusionbrain.ai"),
                        "X-Key", singletonList(fusionToken)
                ));
    }

    @PostConstruct
    public void post() {
        ResponseEntity<ArrayNode> response = restTemplate.exchange(
                fusionUrl + "models", HttpMethod.GET,
                new HttpEntity<>(headers), ArrayNode.class);
//        Preconditions.checkArgument(response.getStatusCode().is2xxSuccessful());
        id = Optional.ofNullable(Utils.safetyGet(() -> response
                .getBody().iterator().next().get("id").asInt())).orElse(id);
    }

    @Override
    public List<String> getCommandLines() {
        return Arrays.asList("/кандинский", "/нарисуй", "/kandinsky");
    }

    @Override
    @SneakyThrows
    public SendPhoto answer(TelegramLongPollingBot bot, Message message) {
        String chatId = message.getChatId().toString();
        if (StringUtils.isBlank(message.getText())) {
            execute(new SendMessage(chatId, "Необходимо описание картинки после команды"), bot);
            return null;
        }
        String format = String.format(answer, message.getText());
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                fusionUrl + "text2image/run", HttpMethod.POST,
                createMultiPartEntity(format.getBytes(StandardCharsets.UTF_8)),
                JsonNode.class);
        Preconditions.checkState(response.getStatusCode().is2xxSuccessful());

        String uuid = response.getBody().get("uuid").asText();


        CompletableFuture.runAsync(() -> {
            int i = 0;
            int max = 5;
            ResponseEntity<JsonNode> r = null;
            do {
                if (i++ > max) break;
                Utils.safetyGet(() -> {
                    Thread.sleep(3_000);
                    return 1;
                });
                r = restTemplate.exchange(
                        fusionUrl + "text2image/status/" + uuid,
                        HttpMethod.GET, new HttpEntity<>(null, headers), JsonNode.class);
                Preconditions.checkState(r.getStatusCode().is2xxSuccessful());
            } while (!r.getBody().get("status").textValue().equals("DONE"));

            String base64 = r.getBody().get("images").iterator().next().asText();
            byte[] bytes = Base64Utils.decodeFromString(base64);

            ByteArrayInputStream mediaStream = new ByteArrayInputStream(requireNonNull(bytes));
            SendPhoto random = new SendPhoto(chatId, new InputFile(mediaStream, "random"));
            random.setCaption(message.getText());
            execute(random, bot);
        });

        return null;
    }

    public HttpEntity<?> createMultiPartEntity(byte[] bytes) {
        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data").name("params").build();

        fileMap.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        fileMap.add(HttpHeaders.CONTENT_TYPE, "boundary=" + generateMultipartBoundaryString());
        HttpEntity<Resource> fileEntity = new HttpEntity<>(new ByteArrayResource(bytes), fileMap);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("params", fileEntity);
        body.add("model_id", id);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.addAll(this.headers);

        return new HttpEntity<>(body, headers);
    }
}
