package restkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class HttpClient {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final Gson gson = new Gson();

    private List<Integer> successCodes = Collections.singletonList(200);

    private String url = "";

    private HttpClientMethod httpMethod = HttpClientMethod.GET;

    private String path = "";

    private Map<String, String> headers = new HashMap<>();

    private Map<String, String> queryParameters = new HashMap<>();

    private Map<String, String> formBody = new HashMap<>();

    private String jsonBody = "";

    private Proxy proxy;

    public HttpClient() {
    }

    public HttpClient setMethod(HttpClientMethod method) {
        this.httpMethod = method;
        return this;
    }

    public HttpClient setUrl(String url) {
        this.url = url;
        return this;
    }

    public HttpClient setPath(String path) {
        this.path = path;
        return this;
    }

    public HttpClient addHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpClient addQueryParameters(Map<String, String> parameters) {
        this.queryParameters = parameters;
        return this;
    }

    public HttpClient addFormBody(Map<String, String> body) {
        this.formBody = body;
        return this;
    }

    public HttpClient addJsonBody(String jsonBody) {
        this.jsonBody = jsonBody;
        return this;
    }

    public HttpClient setSuccessCodes(List<Integer> codes) {
        this.successCodes = codes;
        return this;
    }

    public HttpClient setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public <T, E> HttpResponse<T, E> execute() throws IOException {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .writeTimeout(5000, TimeUnit.MILLISECONDS);

        Request.Builder requestBuilder = new Request.Builder();

        HttpUrl.Builder httpUrlBuilder = new HttpUrl.Builder();
        RequestBody requestBody = null;

        if (url.isEmpty()) {
            throw new HttpClientException(HttpClientError.URL_INVALID.getMessage());
        }

        if (!queryParameters.isEmpty()) {
            for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
                httpUrlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        if (!headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder = requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        if (!formBody.isEmpty()) {
            FormBody.Builder bodyBuilder = new FormBody.Builder();
            for (Map.Entry<String, String> entry : formBody.entrySet()) {
                bodyBuilder = bodyBuilder.add(entry.getKey(), entry.getValue());
            }
            requestBody = bodyBuilder.build();
        }

        if (!jsonBody.isEmpty()) {
            requestBody = RequestBody.create(JSON, jsonBody);
        }

        requestBuilder = requestBuilder.url(url);
        if (httpMethod != HttpClientMethod.GET) {
            if (requestBody != null)
                switch (httpMethod) {
                    case POST:
                        requestBuilder = requestBuilder.post(requestBody);
                    case PUT:
                        requestBuilder = requestBuilder.put(requestBody);
                    case PATCH:
                        requestBuilder = requestBuilder.patch(requestBody);
                    case DELETE:
                        requestBuilder = requestBuilder.delete(requestBody);
                    default:
                        throw new HttpClientException(HttpClientError.METHOD_INVALID.getMessage());
                }
        }

        if (proxy != null) {
            clientBuilder = clientBuilder.proxy(proxy);
        }

        try (Response response = clientBuilder.build().newCall(requestBuilder.build()).execute()) {
            int code = response.code();
            if (successCodes.stream().noneMatch(r -> r.equals(code))) {
                Type collectionType = new TypeToken<E>() {
                }.getType();
                E error = gson.fromJson(Objects.requireNonNull(response.body()).string(), collectionType);
                return new HttpResponse<>(code, null, error);
            }

            Type collectionType = new TypeToken<T>() {
            }.getType();
            T data = gson.fromJson(Objects.requireNonNull(response.body()).string(), collectionType);
            return new HttpResponse<>(code, data, null);
        }
    }
}
