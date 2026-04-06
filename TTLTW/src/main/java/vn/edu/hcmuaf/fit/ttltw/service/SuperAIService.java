package vn.edu.hcmuaf.fit.ttltw.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Properties;

public class SuperAIService {
    private static String API_TOKEN;
    private static String API_URL;
        static{
        try (InputStream input = SuperAIService.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            API_TOKEN = prop.getProperty("supership.api.token");
            API_URL = prop.getProperty("supership.api.url");
        } catch (IOException ex) {
            ex.printStackTrace();
        }}

    public String createRealOrder(int orderId, String name, String phone, String address, double amount) {
        try {
            String jsonBody = new Gson().toJson(Map.of(
                    "order_id", "DH" + orderId,
                    "receiver_name", name,
                    "receiver_phone", phone,
                    "receiver_address", address,
                    "cod", amount
            ));
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_TOKEN)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
            if (obj.get("status").getAsString().equals("Success")) {
                return obj.getAsJsonObject("results").get("code").getAsString();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}
