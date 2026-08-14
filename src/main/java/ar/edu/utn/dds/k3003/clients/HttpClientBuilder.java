package ar.edu.utn.dds.k3003.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientBuilder {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    private static final HttpClient httpClient = HttpClient
            .newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public static <T> T get(String url, Class<T> clazz) throws Exception {
        return sendBody(url, null, clazz, "GET");
    }

    public static <T, G> T post(String url, G body, Class<T> clazz) throws Exception {
        return sendBody(url, body, clazz, "POST");
    }

    public static <T, G> T patch(String url, G body, Class<T> clazz) throws Exception {
        return sendBody(url, body, clazz, "PATCH");
    }

    public static <T> T get(String url, TypeReference<T> typeRef) throws Exception {
        return sendBody(url, null, typeRef, "GET");
    }

    public static <T, G> T post(String url, G body, TypeReference<T> typeRef) throws Exception {
        return sendBody(url, body, typeRef, "POST");
    }

    public static <T, G> T patch(String url, G body, TypeReference<T> typeRef) throws Exception {
        return sendBody(url, body, typeRef, "PATCH");
    }

    private static <T, G> T sendBody(String url, G body, Class<T> clazz, String method) throws Exception {
        HttpRequest request = prepareRequest(url, body, method);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), clazz);
    }

    private static <T, G> T sendBody(String url, G body, TypeReference<T> typeRef, String method) throws Exception {
        HttpRequest request = prepareRequest(url, body, method);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), typeRef);
    }

    private static <G> HttpRequest prepareRequest(String url, G body, String method) throws Exception {
        if ("GET".equalsIgnoreCase(method)) {
            return builder(url).GET().build();
        }
        String json = objectMapper.writeValueAsString(body);
        return builder(url)
                .header("Content-Type", "application/json") // Added required content-type header for POST/PATCH
                .method(method, HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private static HttpRequest.Builder builder(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json");
    }

    public static <G> void postWithoutResponse(String url, G body) throws Exception {

        HttpRequest request = prepareRequest(url, body, "POST");

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}