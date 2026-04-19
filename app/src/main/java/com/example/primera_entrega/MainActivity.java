package com.example.primera_entrega;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements GameAdapter.OnGameClickListener {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    private RecyclerView recyclerView;
    private GameAdapter adapter;
    private List<Game> gameList = new ArrayList<>();
    private FloatingActionButton fab;
    private ProgressBar progressBar;
    private String filtroActivo = "Todos";
    private SessionManager sessionManager;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sessionManager = new SessionManager(this);
        NotificationHelper.createNotificationChannel(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressBar = new ProgressBar(this);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> openAddGameActivity());

        adapter = new GameAdapter(this, gameList, this);
        recyclerView.setAdapter(adapter);

        loadGames();
        setupFiltros();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGames();
    }

    private void loadGames() {
        executor.execute(() -> {
            try {
                String response = ApiClient.getJuegos(sessionManager.getUserId());
                JSONObject json = new JSONObject(response);

                List<Game> todos = new ArrayList<>();
                if (json.getBoolean("success")) {
                    JSONArray array = json.getJSONArray("juegos");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject j = array.getJSONObject(i);
                        Game game = new Game(
                                j.getInt("id"),
                                j.getString("nombre"),
                                j.getString("plataforma"),
                                j.getString("estado"),
                                (float) j.getDouble("horas_jugadas"),
                                j.getInt("puntuacion"),
                                j.getString("notas"),
                                j.getString("fecha_ultima_sesion")
                        );
                        todos.add(game);
                    }
                }

                List<Game> filtrados;
                if (filtroActivo.equals("Todos")) {
                    filtrados = todos;
                } else {
                    filtrados = new ArrayList<>();
                    for (Game g : todos) {
                        if (g.getEstado().equals(filtroActivo)) filtrados.add(g);
                    }
                }

                final List<Game> resultado = filtrados;
                runOnUiThread(() -> {
                    gameList = resultado;
                    adapter.updateList(gameList);
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error cargando juegos", Toast.LENGTH_SHORT).show());
            }
        });
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
                    executor.execute(() -> {
                        try {
                            ApiClient.deleteJuego(game.getId(), sessionManager.getUserId());
                            runOnUiThread(() -> {
                                loadGames();
                                Toast.makeText(this, "Juego eliminado", Toast.LENGTH_SHORT).show();
                            });
                        } catch (Exception e) {
                            runOnUiThread(() ->
                                    Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show());
                        }
                    });
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
        int jugando = 0, completados = 0, abandonados = 0;
        for (Game g : gameList) {
            switch (g.getEstado()) {
                case "Jugando": jugando++; break;
                case "Completado": completados++; break;
                case "Abandonado": abandonados++; break;
            }
        }

        String msg = "Tu GameLog\n\n" +
                "Total: " + gameList.size() + " juegos\n" +
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