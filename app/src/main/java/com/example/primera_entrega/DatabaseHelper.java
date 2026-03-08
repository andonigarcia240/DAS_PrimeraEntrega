package com.example.primera_entrega;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "gamelog.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_GAMES = "games";
    public static final String COL_ID = "id";
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_PLATAFORMA = "plataforma";
    public static final String COL_ESTADO = "estado";
    public static final String COL_HORAS = "horas_jugadas";
    public static final String COL_PUNTUACION = "puntuacion";
    public static final String COL_NOTAS = "notas";
    public static final String COL_FECHA = "fecha_ultima_sesion";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_GAMES + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NOMBRE + " TEXT NOT NULL, " +
                    COL_PLATAFORMA + " TEXT, " +
                    COL_ESTADO + " TEXT, " +
                    COL_HORAS + " REAL, " +
                    COL_PUNTUACION + " INTEGER, " +
                    COL_NOTAS + " TEXT, " +
                    COL_FECHA + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMES);
        onCreate(db);
    }

    // INSERT
    public long insertGame(Game game) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NOMBRE, game.getNombre());
        values.put(COL_PLATAFORMA, game.getPlataforma());
        values.put(COL_ESTADO, game.getEstado());
        values.put(COL_HORAS, game.getHorasJugadas());
        values.put(COL_PUNTUACION, game.getPuntuacion());
        values.put(COL_NOTAS, game.getNotas());
        values.put(COL_FECHA, game.getFechaUltimaSesion());
        long id = db.insert(TABLE_GAMES, null, values);
        db.close();
        return id;
    }

    // GET ALL
    public List<Game> getAllGames() {
        List<Game> games = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_GAMES + " ORDER BY " + COL_FECHA + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                Game game = new Game(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_PLATAFORMA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(COL_HORAS)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_PUNTUACION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTAS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA))
                );
                games.add(game);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return games;
    }

    // GET BY ID
    public Game getGameById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GAMES, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Game game = null;
        if (cursor.moveToFirst()) {
            game = new Game(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PLATAFORMA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(COL_HORAS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_PUNTUACION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTAS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA))
            );
        }
        cursor.close();
        db.close();
        return game;
    }

    // UPDATE
    public int updateGame(Game game) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NOMBRE, game.getNombre());
        values.put(COL_PLATAFORMA, game.getPlataforma());
        values.put(COL_ESTADO, game.getEstado());
        values.put(COL_HORAS, game.getHorasJugadas());
        values.put(COL_PUNTUACION, game.getPuntuacion());
        values.put(COL_NOTAS, game.getNotas());
        values.put(COL_FECHA, game.getFechaUltimaSesion());
        int rows = db.update(TABLE_GAMES, values, COL_ID + "=?",
                new String[]{String.valueOf(game.getId())});
        db.close();
        return rows;
    }

    // DELETE
    public void deleteGame(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GAMES, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // GET COUNT BY ESTADO
    public int countByEstado(String estado) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_GAMES + " WHERE " + COL_ESTADO + "=?",
                new String[]{estado});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }
}