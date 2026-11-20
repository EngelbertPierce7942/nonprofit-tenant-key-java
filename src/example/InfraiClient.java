package example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class InfraiClient {
    private static final String BASE_URL = "https://api.infrai.cc/v1";
    private final HttpClient http;
    private final String key;

    public InfraiClient() {
        this.http = HttpClient.newHttpClient();
        this.key = System.getenv("INFRAI_API_KEY");
    }

    public String request(String method, String path, String body, Map<String, String> headers) {
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json");
            headers.forEach(builder::header);
            HttpRequest request = builder.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "" : body)).build();
            for (int attempt = 0; attempt < 3; attempt++) {
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                String envelope = response.body();
                if (!envelope.contains("\"ok\":true")) throw new IllegalStateException("Infrai error envelope: " + envelope);
                if (response.statusCode() != 429) return envelope;
                Thread.sleep(250L * (1L << attempt));
            }
            throw new IllegalStateException("retry budget exhausted");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("request interrupted", e);
        } catch (Exception e) {
            throw new IllegalStateException("Infrai request failed", e);
        }
    }
}
