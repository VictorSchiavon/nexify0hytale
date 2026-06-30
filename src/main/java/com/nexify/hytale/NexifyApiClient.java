package com.nexify.hytale;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Talks to the Nexify store-delivery endpoints documented by the user:
 *   GET  {base}/store/deliveries/pending/:api_token
 *   POST {base}/store/deliveries/complete/:api_token   body: {"deliveryId": "..."}
 */
public class NexifyApiClient {

    private final NexifyConfig config;
    private final NexifyLog logger;
    private final HttpClient http;
    private final Gson gson = new Gson();

    public NexifyApiClient(NexifyConfig config, NexifyLog logger) {
        this.config = config;
        this.logger = logger;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<PendingDelivery> fetchPendingDeliveries() {
        String url = config.apiBaseUrl + "/store/deliveries/pending/" + config.apiToken;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.warn("[Nexify] Pending deliveries request failed: HTTP " + response.statusCode());
                return List.of();
            }

            // ASSUMPTION: response body is a JSON array of PendingDelivery objects.
            // If the real API wraps it as {"data": [...]}, adjust the type token accordingly.
            List<PendingDelivery> deliveries = gson.fromJson(response.body(), new TypeToken<List<PendingDelivery>>() {}.getType());
            return deliveries != null ? deliveries : List.of();
        } catch (IOException | InterruptedException e) {
            logger.warn("[Nexify] Failed to fetch pending deliveries", e);
            return List.of();
        }
    }

    public boolean completeDelivery(String deliveryId) {
        String url = config.apiBaseUrl + "/store/deliveries/complete/" + config.apiToken;
        String body = gson.toJson(new CompletePayload(deliveryId));
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                logger.warn("[Nexify] Complete delivery " + deliveryId + " failed: HTTP " + response.statusCode());
                return false;
            }
            return true;
        } catch (IOException | InterruptedException e) {
            logger.warn("[Nexify] Failed to complete delivery " + deliveryId, e);
            return false;
        }
    }

    private static class CompletePayload {
        final String deliveryId;
        CompletePayload(String deliveryId) { this.deliveryId = deliveryId; }
    }
}
