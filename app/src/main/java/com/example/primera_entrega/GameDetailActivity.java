package com.example.primera_entrega;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameDetailActivity extends AppCompatActivity {

    private EditText etNombre, etPlataforma, etHoras, etPuntuacion, etNotas;
    private Spinner spinnerEstado;
    private Button btnGuardar, btnBuscarWeb;
    private SessionManager sessionManager;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private int gameId = -1;

    private final String[] estados = {"Jugando", "Completado", "Abandonado"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sessionManager = new SessionManager(this);

        etNombre = findViewById(R.id.etNombre);
        etPlataforma = findViewById(R.id.etPlataforma);
        etHoras = findViewById(R.id.etHoras);
        etPuntuacion = findViewById(R.id.etPuntuacion);
        etNotas = findViewById(R.id.etNotas);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnBuscarWeb = findViewById(R.id.btnBuscarWeb);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, estados);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(spinnerAdapter);

        gameId = getIntent().getIntExtra("game_id", -1);
        if (gameId != -1) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Editar juego");
            loadGameData();
        } else {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Nuevo juego");
        }

        btnGuardar.setOnClickListener(v -> saveGame());

        btnBuscarWeb.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Escribe el nombre del juego primero",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=" +
                            Uri.encode(nombre + " videojuego")));
            startActivity(intent);
        });
    }

    private void loadGameData() {
        executor.execute(() -> {
            try {
                String response = ApiClient.getJuegos(sessionManager.getUserId());
                JSONObject json = new JSONObject(response);
                if (json.getBoolean("success")) {
                    var array = json.getJSONArray("juegos");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject j = array.getJSONObject(i);
                        if (j.getInt("id") == gameId) {
                            runOnUiThread(() -> {
                                try {
                                    etNombre.setText(j.getString("nombre"));
                                    etPlataforma.setText(j.getString("plataforma"));
                                    etHoras.setText(String.valueOf(j.getDouble("horas_jugadas")));
                                    etPuntuacion.setText(String.valueOf(j.getInt("puntuacion")));
                                    etNotas.setText(j.getString("notas"));
                                    String estado = j.getString("estado");
                                    for (int k = 0; k < estados.length; k++) {
                                        if (estados[k].equals(estado)) {
                                            spinnerEstado.setSelection(k);
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error cargando juego", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void saveGame() {
        String nombre = etNombre.getText().toString().trim();
        String plataforma = etPlataforma.getText().toString().trim();
        String estado = spinnerEstado.getSelectedItem().toString();
        String horasStr = etHoras.getText().toString().trim();
        String puntuacionStr = etPuntuacion.getText().toString().trim();
        String notas = etNotas.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            return;
        }

        float horas = horasStr.isEmpty() ? 0 : Float.parseFloat(horasStr);
        int puntuacion = puntuacionStr.isEmpty() ? 0 : Integer.parseInt(puntuacionStr);

        if (puntuacion < 0 || puntuacion > 10) {
            etPuntuacion.setError("La puntuación debe ser entre 0 y 10");
            return;
        }

        String fecha = new SimpleDateFormat("dd/MM/yyyy",
                Locale.getDefault()).format(new Date());

        btnGuardar.setEnabled(false);

        executor.execute(() -> {
            try {
                String response;
                if (gameId == -1) {
                    response = ApiClient.addJuego(sessionManager.getUserId(),
                            nombre, plataforma, estado, horas, puntuacion, notas, fecha);
                } else {
                    response = ApiClient.updateJuego(gameId, sessionManager.getUserId(),
                            nombre, plataforma, estado, horas, puntuacion, notas, fecha);
                }

                JSONObject json = new JSONObject(response);
                runOnUiThread(() -> {
                    btnGuardar.setEnabled(true);
                    if (json.optBoolean("success")) {
                        if (gameId == -1) {
                            NotificationHelper.sendGameAddedNotification(this, nombre);
                        } else if (estado.equals("Completado")) {
                            NotificationHelper.sendGameCompletedNotification(this, nombre);
                        }
                        Toast.makeText(this, "Juego guardado", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnGuardar.setEnabled(true);
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}