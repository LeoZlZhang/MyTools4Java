package leo.tools.not_organized;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson.JacksonFactory;
import leo.tools.object_convert.JacksonKits;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static leo.tools.not_organized.NpeHelper.ensure;

/**
 * Created by sh00514 on 2017/8/10.
 * Http client
 */
@SuppressWarnings("unused")
public class HttpQueryService {

    private final HttpTransport httpTransport = new NetHttpTransport();
    private final HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
    private ThreadLocal<HttpRequest> httpRequestThreadLocal = new ThreadLocal<>();

    public HttpQueryService get(String url) {
        return request("GET", url);
    }

    public HttpQueryService post(String url) {
        return request("POST", url);
    }

    private HttpQueryService request(String method, String url) {
        try {
            this.httpRequestThreadLocal.set(requestFactory.buildRequest(method, new GenericUrl(url), null));
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpQueryService peek(Consumer<HttpRequest> modifier) {
        modifier.accept(httpRequestThreadLocal.get());
        return this;
    }

    public HttpQueryService param(String key, Object value) {
        this.httpRequestThreadLocal.get().getUrl().set(key, value);
        return this;
    }

    public HttpQueryService param(Map<String, Object> map) {
        GenericUrl url = this.httpRequestThreadLocal.get().getUrl();
        map.forEach(url::set);
        return this;
    }

    public HttpQueryService header(String key, Object value) {
        this.httpRequestThreadLocal.get().getHeaders().set(key, value);
        return this;
    }

    public HttpQueryService header(Map<String, Object> map) {
        HttpHeaders headers = this.httpRequestThreadLocal.get().getHeaders();
        map.forEach(headers::put);
        return this;
    }

    public HttpQueryService jsonBody(Map<String, Object> map) {
        this.httpRequestThreadLocal.get().setContent(new JsonHttpContent(new JacksonFactory(), map));
        return this;
    }

    public HttpQueryService urlencodedBody(Map<String, Object> map) {
        this.httpRequestThreadLocal.get().setContent(new UrlEncodedContent(map));
        return this;
    }

    public HttpQueryService multiPartTextBody(Map<String, String> map) {
        MultipartContent content = Optional.ofNullable(this.httpRequestThreadLocal.get())
                .map(HttpRequest::getContent)
                .filter(t -> t instanceof MultipartContent)
                .map(t -> (MultipartContent) t)
                .orElseGet(() -> {
                    MultipartContent multipartContent = new MultipartContent();
                    multipartContent.setMediaType(new HttpMediaType("multipart/form-data").setParameter("boundary", "__END_OF_PART__"));
                    return multipartContent;
                });

        map.entrySet().stream()
                .map(set -> {
                    MultipartContent.Part part = new MultipartContent.Part();
                    part.setEncoding(null);
                    part.setHeaders(new HttpHeaders().set("Content-Disposition", String.format("form-data; name=\"%s\"", set.getKey())));
                    part.setContent(ByteArrayContent.fromString(null, set.getValue()));
                    return part;
                })
                .forEach(content::addPart);

        this.httpRequestThreadLocal.get().setContent(content);

        return this;
    }

    public HttpQueryService multiPartBinaryBody(String name, String fileName, String mimeType, File file) {
        MultipartContent content = Optional.ofNullable(this.httpRequestThreadLocal.get())
                .map(HttpRequest::getContent)
                .filter(t -> t instanceof MultipartContent)
                .map(t -> (MultipartContent) t)
                .orElseGet(() -> {
                    MultipartContent multipartContent = new MultipartContent();
                    multipartContent.setMediaType(new HttpMediaType("multipart/form-data").setParameter("boundary", "__END_OF_PART__"));
                    return multipartContent;
                });

        MultipartContent.Part part = new MultipartContent.Part();
        part.setEncoding(null);
        part.setHeaders(new HttpHeaders().set("Content-Disposition", String.format("form-data; name=\"%s\"; filename=\"%s\"", name, fileName)));
        part.setContent(new FileContent(mimeType, file));

        content.addPart(part);

        this.httpRequestThreadLocal.get().setContent(content);

        return this;
    }

    public String execute() {
        HttpRequest request = ensure(httpRequestThreadLocal.get());
        try {
            HttpResponse response = request.execute();
            if (response.getStatusCode() > 300)
                throw new RuntimeException("bad request:" + response.getStatusCode());
            try (InputStream is = response.getContent()) {
                return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            httpRequestThreadLocal.remove();
        }
    }

    public <T> Optional<T> execute(Class<T> cls) {
        return JacksonKits.fromJson(execute(), cls);
    }
}
