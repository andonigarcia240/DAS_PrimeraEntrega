package com.example.primera_entrega;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GameAdapter.OnGameClickListener {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    private RecyclerView recyclerView;
    private GameAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Game> gameList;
    private FloatingActionButton fab;
    private SharedPreferences prefs;
    private String filtroActivo = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = getSharedPreferences("gamelog_prefs", MODE_PRIVATE);
        dbHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> openAddGameActivity());

        // Pedir permiso de notificaciones en Android 13+
        requestNotificationPermission();

        loadGames();
        setupFiltros();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            } else {
                NotificationHelper.createNotificationChannel(this);
            }
        } else {
            NotificationHelper.createNotificationChannel(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                NotificationHelper.createNotificationChannel(this);
            } else {
                Toast.makeText(this,
                        "Sin permisos no se mostrarán notificaciones",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGames();
    }

    private void loadGames() {
        List<Game> todos = dbHelper.getAllGames();
        if (filtroActivo.equals("Todos")) {
            gameList = todos;
        } else {
            gameList = new ArrayList<>();
            for (Game g : todos) {
                if (g.getEstado().equals(filtroActivo)) gameList.add(g);
            }
        }
        if (adapter == null) {
            adapter = new GameAdapter(this, gameList, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(gameList);
        }
    }

    private void setupFiltros() {
        TextView filtroTodos = findViewById(R.id.filtroTodos);
        TextView filtroJugando = findViewById(R.id.filtroJugando);
        TextView filtroCompletado = findViewById(R.id.filtroCompletado);
        TextView filtroAbandonado = findViewById(R.id.filtroAbandonado);

        List<TextView> chips = Arrays.asList(filtroTodos, filtroJugando,
                filtroCompletado, filtroAbandonado);
        List<String> valores = Arrays.asList("Todos", "Jugando",
                "Completado", "Abandonado");

        for (int i = 0; i < chips.size(); i++) {
            final String valor = valores.get(i);
            chips.get(i).setOnClickListener(v -> {
                filtroActivo = valor;
                for (TextView chip : chips) {
                    chip.setBackgroundResource(R.drawable.chip_unselected);
                }
                filtroTodos.setTextColor(getResources().getColor(android.R.color.white, null));
                filtroJugando.setTextColor(0xFF7CBFA0);
                filtroCompletado.setTextColor(0xFF7C9FD4);
                filtroAbandonado.setTextColor(0xFFD47C7C);

                ((TextView) v).setBackgroundResource(R.drawable.chip_selected);
                ((TextView) v).setTextColor(getResources().getColor(android.R.color.white, null));
                loadGames();
            });
        }
    }

    private void openAddGameActivity() {
        Intent intent = new Intent(this, GameDetailActivity.class);
        startActivity(intent);
    }

    @Override
    public void onGameClick(Game game) {
        Intent intent = new Intent(this, GameDetailActivity.class);
        intent.putExtra("game_id", game.getId());
        startActivity(intent);
    }

    @Override
    public void onGameLongClick(Game game) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar juego")
                .setMessage("¿Eliminar \"" + game.getNombre() + "\" de tu GameLog?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    dbHelper.deleteGame(game.getId());
                    loadGames();
                    Toast.makeText(this, "Juego eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_stats) {
            showStatsDialog();
            return true;
        } else if (id == R.id.action_search_web) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=videojuegos+recomendados"));
            startActivity(intent);
            return true;
        } else if (id == R.id.action_map) {
        startActivity(new Intent(this, MapActivity.class));
        return true;
        } else if (id == R.id.action_profile) {
        startActivity(new Intent(this, ProfileActivity.class));
        return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showStatsDialog() {
        int jugando = dbHelper.countByEstado("Jugando");
        int completados = dbHelper.countByEstado("Completado");
        int abandonados = dbHelper.countByEstado("Abandonado");
        int total = gameList.size();

        String msg = "Tu GameLog\n\n" +
                "Total: " + total + " juegos\n" +
                "Jugando: " + jugando + "\n" +
                "Completados: " + completados + "\n" +
                "Abandonados: " + abandonados;

        new AlertDialog.Builder(this)
                .setTitle("Estadísticas")
                .setMessage(msg)
                .setPositiveButton("Cerrar", null)
                .show();
    }
}