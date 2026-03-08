package com.example.primera_entrega;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GameAdapter.OnGameClickListener {

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
        NotificationHelper.createNotificationChannel(this);
        dbHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> openAddGameActivity());

        loadGames();
        setupFiltros();
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

        List<TextView> chips = Arrays.asList(filtroTodos, filtroJugando, filtroCompletado, filtroAbandonado);
        List<String> valores = Arrays.asList("Todos", "Jugando", "Completado", "Abandonado");

        for (int i = 0; i < chips.size(); i++) {
            final String valor = valores.get(i);
            chips.get(i).setOnClickListener(v -> {
                filtroActivo = valor;
                for (TextView chip : chips) {
                    chip.setBackgroundResource(R.drawable.chip_unselected);
                }
                // Restaurar colores de texto de cada chip
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