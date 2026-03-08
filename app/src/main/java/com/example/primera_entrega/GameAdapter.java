package com.example.primera_entrega;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<Game> gameList;
    private Context context;
    private OnGameClickListener listener;

    public interface OnGameClickListener {
        void onGameClick(Game game);
        void onGameLongClick(Game game);
    }

    public GameAdapter(Context context, List<Game> gameList, OnGameClickListener listener) {
        this.context = context;
        this.gameList = gameList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = gameList.get(position);

        holder.tvNombre.setText(game.getNombre());
        holder.tvPlataforma.setText(game.getPlataforma());
        holder.tvHoras.setText(game.getHorasJugadas() + "h jugadas");
        holder.tvPuntuacion.setText(game.getPuntuacion() + "/10");
        holder.tvFecha.setText(game.getFechaUltimaSesion());
        holder.tvEstado.setText(game.getEstado());

        int color;
        switch (game.getEstado()) {
            case "Completado":
                color = 0xFF7C9FD4;
                break;
            case "Abandonado":
                color = 0xFFD47C7C;
                break;
            default:
                color = 0xFF7CBFA0;
                break;
        }
        holder.tvEstado.setTextColor(color);
        holder.tvEstado.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        holder.barraEstado.setBackgroundColor(color);

        holder.cardView.setOnClickListener(v -> listener.onGameClick(game));
        holder.cardView.setOnLongClickListener(v -> {
            listener.onGameLongClick(game);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public void updateList(List<Game> newList) {
        this.gameList = newList;
        notifyDataSetChanged();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        View barraEstado;
        TextView tvNombre, tvPlataforma, tvEstado, tvHoras, tvPuntuacion, tvFecha;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            barraEstado = itemView.findViewById(R.id.barraEstado);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvPlataforma = itemView.findViewById(R.id.tvPlataforma);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvHoras = itemView.findViewById(R.id.tvHoras);
            tvPuntuacion = itemView.findViewById(R.id.tvPuntuacion);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }
}