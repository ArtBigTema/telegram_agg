package art.vas.telegram.fact.config;

import art.vas.telegram.fact.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Integer BINARY_LOGGING_BODY_MAX_LENGTH_BYTES = 500_000;

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        debugRequest(Objects.requireNonNull(request), body);

        ClientHttpResponse response = execution.execute(request, Objects.requireNonNull(body));

        byte[] bytes = Utils.safetyGet(()-> IOUtils.toByteArray(response.getBody()));
        debugResponse(response, bytes);

        if (response.getStatusCode().isError()) {
            log.error("HTTP Status Code: " + response.getStatusCode().value());
            throw new IllegalStateException(new String(ObjectUtils.defaultIfNull(bytes, ArrayUtils.EMPTY_BYTE_ARRAY)));
        }

        return getClientHttpResponse(response, bytes);
    }

    private void debugRequest(HttpRequest request, byte[] body) {
        log.info("""
                            
                        ============request begin============
                        URI : {}
                        Method : {}
                        Headers : {}
                        Body: {}
                        =============request end=============""",
                request.getURI(), request.getMethod(), request.getHeaders(),
                getReadableBody(body, request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)));
    }

    private void debugResponse(ClientHttpResponse response, byte[] body) throws IOException {
        log.info("""
                            
                        ============response begin============
                        Status : {}
                        Headers : {}
                        Body: {}
                        =============response end=============""",
                response.getStatusCode(), response.getHeaders(),
                getReadableBody(body, response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)));
    }

    private String getReadableBody(byte[] body, String contentType) {
        if (Objects.isNull(body) || body.length == 0) return StringUtils.EMPTY;
        //textual value
        if (StringUtils.containsAny(contentType, "text", "html", "xml", "json")) {
            return new String(body, StandardCharsets.UTF_8);
        }
        if (StringUtils.startsWith(contentType, "image/")) {
            return "Image ...";
        }
        if (body.length > BINARY_LOGGING_BODY_MAX_LENGTH_BYTES) {
            return "Binary body more them 0.5MB (too long to log)";
        }
        return "Base64 - " + Base64.getEncoder().encodeToString(body);
    }

    private static ClientHttpResponse getClientHttpResponse(ClientHttpResponse response, byte[] bytes) {
        return new ClientHttpResponse() {


            @Override
            public HttpStatusCode getStatusCode() throws IOException {
                return response.getStatusCode();
            }

            @Override
            public String getStatusText() throws IOException {
                return response.getStatusText();
            }

            @Override
            public void close() {
                response.close();
            }

            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public HttpHeaders getHeaders() {
                return response.getHeaders();
            }
        };
    }
}
