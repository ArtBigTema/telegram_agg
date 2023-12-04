package art.vas.telegram.fact.config;

import art.vas.telegram.fact.service.ProxyService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.CastUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.apache.http.HttpHeaders.HOST;

@Configuration
@RequiredArgsConstructor
public class JsonConfig {

    @Value("${panda.proxy.token}")
    String pandaProxyToken;
    private final ProxyService proxyService;
    public static final RestTemplate restTemplate = new RestTemplate();

    static {
        restTemplate.setInterceptors(Collections.singletonList(new LoggingInterceptor()));
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    @Bean
    public RestTemplate proxyRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
            @Override
            @SneakyThrows
            public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) {//todo fixme
                Pair<Proxy, URL> pair = proxyService.getPair(uri);
                HttpURLConnection connection = openConnection(pair.getValue(), pair.getKey());
                prepareConnection(connection, httpMethod.name());
                String host = pair.getValue().getHost();
                connection.setRequestProperty(HOST, host);
                if (StringUtils.contains(host, "panda")) {//todo
                    connection.setRequestProperty("x-usagepanda-api-key", pandaProxyToken);
                }

                Class<?> aClass = ClassUtils.getClass("org.springframework.http.client.SimpleClientHttpRequest");
                Constructor<?> declaredConstructor = aClass.getDeclaredConstructor(HttpURLConnection.class, int.class);
                ReflectionUtils.makeAccessible(declaredConstructor);
                return CastUtils.cast(declaredConstructor.newInstance(connection, 4096));
            }
        };
        requestFactory.setConnectTimeout(60_000);
        requestFactory.setReadTimeout(60_000);
        RestTemplate template = new RestTemplate(requestFactory);
        template.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
//        template.setInterceptors(Collections.singletonList(proxyService));

        return template;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
