package com.example.primera_entrega;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {

    private static final String BASE_URL = "http://104.199.16.252/gamelog/";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    public static String registro(String username, String email, String password) throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("email", email)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "registro.php")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String login(String email, String password) throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "login.php")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}