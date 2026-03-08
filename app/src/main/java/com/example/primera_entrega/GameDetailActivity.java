package com.example.primera_entrega;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GameDetailActivity extends AppCompatActivity {

    private EditText etNombre, etPlataforma, etHoras, etPuntuacion, etNotas;
    private Spinner spinnerEstado;
    private Button btnGuardar, btnBuscarWeb;
    private DatabaseHelper dbHelper;
    private int gameId = -1;

    private final String[] estados = {"Jugando", "Completado", "Abandonado"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new DatabaseHelper(this);

        etNombre = findViewById(R.id.etNombre);
        etPlataforma = findViewById(R.id.etPlataforma);
        etHoras = findViewById(R.id.etHoras);
        etPuntuacion = findViewById(R.id.etPuntuacion);
        etNotas = findViewById(R.id.etNotas);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnBuscarWeb = findViewById(R.id.btnBuscarWeb);

        // Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, estados);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(spinnerAdapter);

        // Modo edición
        gameId = getIntent().getIntExtra("game_id", -1);
        if (gameId != -1) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Editar juego");
            loadGameData();
        } else {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Nuevo juego");
        }

        btnGuardar.setOnClickListener(v -> saveGame());

        // Intent implícito: buscar el juego en Google
        btnBuscarWeb.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Escribe el nombre del juego primero", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=" + Uri.encode(nombre + " videojuego")));
            startActivity(intent);
        });
    }

    private void loadGameData() {
        Game game = dbHelper.getGameById(gameId);
        if (game != null) {
            etNombre.setText(game.getNombre());
            etPlataforma.setText(game.getPlataforma());
            etHoras.setText(String.valueOf(game.getHorasJugadas()));
            etPuntuacion.setText(String.valueOf(game.getPuntuacion()));
            etNotas.setText(game.getNotas());

            for (int i = 0; i < estados.length; i++) {
                if (estados[i].equals(game.getEstado())) {
                    spinnerEstado.setSelection(i);
                    break;
                }
            }
        }
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

        String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        Game game = new Game(gameId == -1 ? 0 : gameId, nombre, plataforma,
                estado, horas, puntuacion, notas, fecha);

        if (gameId == -1) {
            dbHelper.insertGame(game);
            NotificationHelper.sendGameAddedNotification(this, nombre);
            Toast.makeText(this, "Juego añadido ✅", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateGame(game);
            if (estado.equals("Completado")) {
                NotificationHelper.sendGameCompletedNotification(this, nombre);
            }
            Toast.makeText(this, "Juego actualizado ✅", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}