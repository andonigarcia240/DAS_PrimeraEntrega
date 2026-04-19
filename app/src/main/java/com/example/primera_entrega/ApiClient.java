package com.example.primera_entrega;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {

    private static final String BASE_URL = "http://34.140.125.55/gamelog/";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    // AUTH
    public static String registro(String username, String email, String password) throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("email", email)
                .add("password", password)
                .build();
        return post("registro.php", body);
    }

    public static String login(String email, String password) throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();
        return post("login.php", body);
    }

    // JUEGOS
    public static String getJuegos(int userId) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "get_juegos.php?user_id=" + userId)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String addJuego(int userId, String nombre, String plataforma,
                                  String estado, float horas, int puntuacion,
                                  String notas, String fecha) throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("user_id", String.valueOf(userId))
                .add("nombre", nombre)
                .add("plataforma", plataforma)
                .add("estado", estado)
                .add("horas_jugadas", String.valueOf(horas))
                .add("puntuacion", String.valueOf(puntuacion))
                .add("notas", notas)
                .add("fecha_ultima_sesion", fecha)
                .build();
        return post("add_juego.php", body);
    }

    public static String updateJuego(int id, int userId, String nombre, String plataforma,
                                     String estado, float horas, int puntuacion,
                                     String notas, String fecha) throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("id", String.valueOf(id))
                .add("user_id", String.valueOf(userId))
                .add("nombre", nombre)
                .add("plataforma", plataforma)
                .add("estado", estado)
                .add("horas_jugadas", String.valueOf(horas))
                .add("puntuacion", String.valueOf(puntuacion))
                .add("notas", notas)
                .add("fecha_ultima_sesion", fecha)
                .build();
        return post("update_juego.php", body);
    }

    public static String deleteJuego(int id, int userId) throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("id", String.valueOf(id))
                .add("user_id", String.valueOf(userId))
                .build();
        return post("delete_juego.php", body);
    }

    private static String post(String endpoint, RequestBody body) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}