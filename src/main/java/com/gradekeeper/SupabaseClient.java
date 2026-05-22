package com.gradekeeper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gradekeeper.models.Class;
import com.gradekeeper.models.Grade;

import io.github.cdimascio.dotenv.Dotenv;

public class SupabaseClient {
    private static SupabaseClient instance;
    private final String supabaseUrl;
    private final String supabaseKey;
    private final HttpClient httpClient;

    private SupabaseClient() {
        Dotenv dotenv = Dotenv.load();
        this.supabaseUrl = dotenv.get("SUPABASE_URL");
        this.supabaseKey = dotenv.get("SUPABASE_KEY");
        this.httpClient = HttpClient.newHttpClient();
    }

    public static SupabaseClient getInstance() {
        if (instance == null) {
            instance = new SupabaseClient();
        }
        return instance;
    }

    private HttpRequest.Builder requestBuilder(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/rest/v1/" + endpoint))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Content-Type", "application/json");
    }

    public List<Class> getClasses() {
        List<Class> classes = new ArrayList<>();
        try {
            HttpRequest request = requestBuilder("classes?select=*&order=name.asc")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            JSONArray jsonArray = new JSONArray(response.body());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                classes.add(new Class(
                    obj.getInt("id"),
                    obj.getString("name"),
                    obj.optString("created_at", "")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    public boolean addClass(String name) {
        try {
            JSONObject body = new JSONObject();
            body.put("name", name);
            
            HttpRequest request = requestBuilder("classes")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return response.statusCode() == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteClass(int classId) {
        try {
            HttpRequest request = requestBuilder("classes?id=eq." + classId)
                    .DELETE()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Grade> getGrades(int classId) {
        List<Grade> grades = new ArrayList<>();
        try {
            HttpRequest request = requestBuilder("grades?select=*&class_id=eq." + classId)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            JSONArray jsonArray = new JSONArray(response.body());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                grades.add(new Grade(
                    obj.getInt("id"),
                    obj.getInt("class_id"),
                    obj.getString("assignment"),
                    obj.getDouble("score"),
                    obj.getDouble("max_points")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return grades;
    }

    public boolean addGrade(int classId, String assignment, double score, double maxPoints) {
        try {
            JSONObject body = new JSONObject();
            body.put("class_id", classId);
            body.put("assignment", assignment);
            body.put("score", score);
            body.put("max_points", maxPoints);
            
            HttpRequest request = requestBuilder("grades")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return response.statusCode() == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteGrade(int gradeId) {
        try {
            HttpRequest request = requestBuilder("grades?id=eq." + gradeId)
                    .DELETE()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Double calculateAverage(int classId) {
        List<Grade> grades = getGrades(classId);
        if (grades.isEmpty()) {
            return null;
        }
        
        double totalScore = 0;
        double totalMax = 0;
        for (Grade grade : grades) {
            totalScore += grade.getScore();
            totalMax += grade.getMaxPoints();
        }
        
        if (totalMax == 0) return 0.0;
        return (totalScore / totalMax) * 100;
    }

    public String getNumericGrade(double percentage) {
        if (percentage >= 97) return "1.0";
        if (percentage >= 94) return "1.25";
        if (percentage >= 91) return "1.5";
        if (percentage >= 88) return "1.75";
        if (percentage >= 85) return "2.0";
        if (percentage >= 82) return "2.25";
        if (percentage >= 79) return "2.5";
        if (percentage >= 76) return "2.75";
        if (percentage >= 75) return "3.0";
        if (percentage >= 65) return "5.0";
        return "5.0";  // below 65 is also failure
    }
    
    public String getGradeDescription(String numericGrade) {
        switch (numericGrade) {
            case "1.0": return "Excellent";
            case "1.25": return "Excellent";
            case "1.5": return "Very Good";
            case "1.75": return "Very Good";
            case "2.0": return "Good";
            case "2.25": return "Good";
            case "2.5": return "Satisfactory";
            case "2.75": return "Satisfactory";
            case "3.0": return "Passing";
            case "5.0": return "Failure";
            default: return "";
        }
    }
}