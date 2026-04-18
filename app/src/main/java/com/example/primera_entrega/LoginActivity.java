package com.example.primera_entrega;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvIrRegistro;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        // Si ya hay sesión activa, ir directamente a MainActivity
        if (sessionManager.isLoggedIn()) {
            goToMain();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvIrRegistro = findViewById(R.id.tvIrRegistro);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> doLogin());

        tvIrRegistro.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        executor.execute(() -> {
            try {
                String response = ApiClient.login(email, password);
                JSONObject json = new JSONObject(response);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    try {
                        if (json.getBoolean("success")) {
                            sessionManager.saveSession(
                                    json.getInt("id"),
                                    json.getString("username"),
                                    email,
                                    json.optString("foto_perfil", "")
                            );
                            goToMain();
                        } else {
                            Toast.makeText(this, json.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error procesando respuesta",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(this, "Error de conexión con el servidor",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}