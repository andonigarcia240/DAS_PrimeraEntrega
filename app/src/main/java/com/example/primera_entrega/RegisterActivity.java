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

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword;
    private Button btnRegistro;
    private TextView tvIrLogin;
    private ProgressBar progressBar;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegistro = findViewById(R.id.btnRegistro);
        tvIrLogin = findViewById(R.id.tvIrLogin);
        progressBar = findViewById(R.id.progressBar);

        btnRegistro.setOnClickListener(v -> doRegister());

        tvIrLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void doRegister() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegistro.setEnabled(false);

        executor.execute(() -> {
            try {
                String response = ApiClient.registro(username, email, password);
                JSONObject json = new JSONObject(response);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegistro.setEnabled(true);

                    try {
                        if (json.getBoolean("success")) {
                            Toast.makeText(this, "Cuenta creada. ¡Inicia sesión!",
                                    Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
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
                    btnRegistro.setEnabled(true);
                    Toast.makeText(this, "Error de conexión con el servidor",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}