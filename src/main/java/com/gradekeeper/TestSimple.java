package com.gradekeeper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import io.github.cdimascio.dotenv.Dotenv;

public class TestSimple {
    public static void main(String[] args) {
        try {
            Dotenv dotenv = Dotenv.load();
            String url = dotenv.get("SUPABASE_URL");
            String key = dotenv.get("SUPABASE_KEY");
            
            System.out.println("Testing Supabase connection...");
            System.out.println("URL: " + url);
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/rest/v1/classes?select=*"))
                    .header("apikey", key)
                    .header("Authorization", "Bearer " + key)
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response code: " + response.statusCode());
            System.out.println("Response body: " + response.body());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}