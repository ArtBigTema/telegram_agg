package art.vas.telegram.fact.service;

import art.vas.telegram.fact.utils.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;
import static org.apache.commons.collections4.CollectionUtils.size;
import static org.apache.commons.lang3.StringUtils.substringAfter;

@Component
@RequiredArgsConstructor
public class ProxyService {
    @Getter
    public static LinkedList<Pair<Proxy, Long>> proxies = new LinkedList<>();


    @Value("${open.ai.url}")
    String openAiUrl;
    @Value("${timeout}")
    Integer timeout;

    @Value("${panda.proxy.url}")
    String pandaAiUrl;

    //    @PostConstruct
    public void after() {
        after(Arrays.asList(
                "45.167.124.170:999",
                "34.77.56.122:8080",
                "86.62.3.159:3128",
                "154.236.189.24:1981",
                "211.226.174.45:8080"
        ));
    }


    public void after(List<String> list) {
        if (CollectionUtils.isEmpty(list)) after();
//        jsonNode = jsonNode.get("data");
        for (String datum : list) {
            InetSocketAddress address = Utils.safetyGet(() -> {
                String ip = StringUtils.split(datum, ":")[0];
                int port = Integer.parseInt(StringUtils.split(datum, ":")[1]);
                return new InetSocketAddress(ip, port);
            });
            if (Objects.isNull(address)) continue;
            Proxy proxy = new Proxy(Proxy.Type.HTTP, address);

            proxies.add(Utils.connect(proxy, () ->
                    Jsoup.connect(openAiUrl)
                            .proxy(proxy).timeout(timeout).get()));
        }
        proxies = proxies.stream()
                .filter(p -> p.getValue() < timeout)
                .sorted(comparingByValue())
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @SneakyThrows
    public Pair<Proxy, URL> getPair(URI uri) {
        String after = substringAfter(uri.toString(), "/v1");
        Pair<Proxy, Long> poll = proxies.peek();
        if (Objects.isNull(poll)) {
            return Pair.of(null, URI.create(pandaAiUrl + after).toURL());
        }
        System.err.println(poll);
        return Pair.of(poll.getKey(), URI.create(openAiUrl + after).toURL());
    }

    public static <V> V repeat(Utils.Supplier<V, Exception> supplier) {
        V v = null;
        int i = 0;
        int max = proxies.size() * 2;
        do {
            if (i++ > max) break;
            try {
                v = supplier.get();
            } catch (Exception e) {
                if (size(proxies) > 1)
                    proxies.add(proxies.pop()); // next proxy
            }
        } while (Objects.isNull(v));
        if (i >= max) {
            proxies.clear();
            v = Utils.safetyGet(supplier);
        }
        return v;
    }

    /*@NotNull
    @Override
    public ClientHttpResponse intercept(@Nullable HttpRequest request, @Nullable byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        try {
            return execution.execute(Objects.requireNonNull(request), Objects.requireNonNull(body));
        } catch (IOException e) {
            if (contains(e.getMessage(), "timeout")) {
                Utils.safetyGet(() -> proxies.pop());
            }
            throw e;
        }
    }*/
}
