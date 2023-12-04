package art.vas.telegram.fact.command;

import art.vas.telegram.fact.command.common.Commando;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.collections4.CollectionUtils;
import org.glassfish.jersey.internal.guava.Preconditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static art.vas.telegram.fact.config.JsonConfig.restTemplate;


@Component
public class PhotoCommand implements Commando<SendPhoto> {
    private static final String unsplash = "https://api.unsplash.com/photos/random?client_id=";

    @Value("${unsplash.self}")
    String utm;
    @Value("${unsplash.access}")
    String token;

    @Override
    public List<String> getCommandLines() {
        return Arrays.asList("/photo", "/фото");
    }

    @Override
    public SendPhoto answer(String chatId) {
        return answer(chatId, Collections.emptyList());
    }

    public SendPhoto answer(String chatId, List<String> keys) {
        String url = unsplash.concat(token).concat(utm);
        url = url.concat(CollectionUtils.emptyIfNull(keys).stream().collect(Collectors.joining(",", "&query=", "")));
        ResponseEntity<JsonNode> forEntity = restTemplate.getForEntity(url, JsonNode.class);
        Preconditions.checkArgument(forEntity.getStatusCode().is2xxSuccessful());

        JsonNode urls = Objects.requireNonNull(forEntity.getBody()).get("urls");
        url = urls.get("small").asText().concat(utm).concat("&client_id=").concat(token);

        byte[] image = Objects.requireNonNull(restTemplate.getForObject(url, byte[].class));
        return new SendPhoto(chatId, new InputFile(new ByteArrayInputStream(image), "random"));
    }
}
