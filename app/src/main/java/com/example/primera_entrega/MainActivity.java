package com.example.primera_entrega;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements GameAdapter.OnGameClickListener {

    private RecyclerView recyclerView;
    private GameAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Game> gameList;
    private FloatingActionButton fab;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Preferencias
        prefs = getSharedPreferences("gamelog_prefs", MODE_PRIVATE);

        // Notificaciones
        NotificationHelper.createNotificationChannel(this);

        // DB
        dbHelper = new DatabaseHelper(this);

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // FAB
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> openAddGameActivity());

        loadGames();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGames();
    }

    private void loadGames() {
        gameList = dbHelper.getAllGames();
        if (adapter == null) {
            adapter = new GameAdapter(this, gameList, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(gameList);
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
            // Intent implícito — buscar en web
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

        String msg = "📊 Tu GameLog\n\n" +
                "Total: " + total + " juegos\n" +
                "🎮 Jugando: " + jugando + "\n" +
                "🏆 Completados: " + completados + "\n" +
                "❌ Abandonados: " + abandonados;

        new AlertDialog.Builder(this)
                .setTitle("Estadísticas")
                .setMessage(msg)
                .setPositiveButton("Cerrar", null)
                .show();
    }
}