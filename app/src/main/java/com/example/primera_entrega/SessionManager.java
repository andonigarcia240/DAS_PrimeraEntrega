package com.example.primera_entrega;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "gamelog_session";
    private static final String KEY_IS_LOGGED = "is_logged";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FOTO = "foto_perfil";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(int userId, String username, String email, String foto) {
        editor.putBoolean(KEY_IS_LOGGED, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_FOTO, foto);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED, false);
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getFoto() {
        return prefs.getString(KEY_FOTO, "");
    }

    public void setFoto(String foto) {
        editor.putString(KEY_FOTO, foto);
        editor.apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}