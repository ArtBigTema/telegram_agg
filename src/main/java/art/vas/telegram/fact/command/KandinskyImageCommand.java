package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.Commando;
import art.vas.telegram.fact.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static art.vas.telegram.fact.config.JsonConfig.restTemplate;
import static java.util.Collections.singletonList;
import static org.springframework.util.MimeTypeUtils.generateMultipartBoundaryString;


@Component
@RequiredArgsConstructor
public class KandinskyImageCommand implements Commando<SendPhoto> {
    public static final String answer = "{\"type\":\"GENERATE\",\"generateParams\":{ \"query\": \"%s\"}}";

    @Value("${fusion.proxy.url}")
    String fusionUrl;
    @Value("${fusion.proxy.token}")
    String fusionToken;
    @Value("${fusion.proxy.secret}")
    String fusionSecret;
    private static int id = 4;

    @PostConstruct
    public void post() {
        HttpEntity<?> entity = new HttpEntity<>(CollectionUtils.toMultiValueMap(
                Map.of("X-Secret", singletonList(fusionSecret),
                        "X-Key", singletonList(fusionToken))));

        ResponseEntity<ArrayNode> response = restTemplate.exchange(
                fusionUrl + "models", HttpMethod.GET, entity, ArrayNode.class);
//        Preconditions.checkArgument(response.getStatusCode().is2xxSuccessful());
        id = Optional.ofNullable(Utils.safetyGet(() -> response
                .getBody().iterator().next().get("id").asInt())).orElse(id);
    }

    @Override
    public String getCommandLine() {
        return "/кандинский";
    }

    @Override
    @SneakyThrows
    public SendPhoto answer(Message message) {
        String chatId = message.getChatId().toString();
        String text = StringUtils.removeStart(message.getText(), getCommandLine());
        String format = String.format(answer, text);
//        JsonNode body = objectMapper.valueToTree(format);

        HttpEntity<?> entity = new HttpEntity<>(
                Map.of("model_id", id, "params", format),
                CollectionUtils.toMultiValueMap(
                        Map.of("X-Secret", singletonList(fusionSecret),
                                "X-Key", singletonList(fusionToken))));

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                fusionUrl + "text2image/run", HttpMethod.POST,
                createMultiPartEntity(format.getBytes(StandardCharsets.UTF_8))
                , JsonNode.class);
        Preconditions.checkState(response.getStatusCode().is2xxSuccessful());

        String uuid = response.getBody().get("uuid").asText();


        int i = 0;
        int max = 5;
        do {
            if (i++ > max) break;
            Thread.sleep(3_000);
            response = restTemplate.exchange(
                    fusionUrl + "text2image/status/" + uuid,
                    HttpMethod.GET, entity, JsonNode.class);
            Preconditions.checkState(response.getStatusCode().is2xxSuccessful());
        } while (!response.getBody().get("status").textValue().equals("DONE"));

        String base64 = response.getBody().get("images").iterator().next().asText();
        byte[] bytes = Base64Utils.decodeFromString(base64);

        return new SendPhoto(chatId, new InputFile(new ByteArrayInputStream(bytes), "random"));
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
        headers.add("X-Secret", fusionSecret);
        headers.add("X-Key", fusionToken);

        return new HttpEntity<>(body, headers);
    }
}
