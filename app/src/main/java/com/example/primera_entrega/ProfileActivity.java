package com.example.primera_entrega;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 3001;
    private static final int REQUEST_CAMERA_PERMISSION = 3002;

    private ImageView ivFotoPerfil;
    private TextView tvUsername, tvEmail;
    private Button btnCamara, btnLogout;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private Uri fotoUri;
    private File fotoFile;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi perfil");
        }

        sessionManager = new SessionManager(this);

        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        btnCamara = findViewById(R.id.btnCamara);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);

        tvUsername.setText(sessionManager.getUsername());
        tvEmail.setText(sessionManager.getEmail());

        // Cargar foto de perfil si existe
        String fotoUrl = sessionManager.getFoto();
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            cargarFotoDesdeServidor(fotoUrl);
        }

        btnCamara.setOnClickListener(v -> checkCameraPermission());

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                fotoFile = crearArchivoFoto();
                fotoUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", fotoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                startActivityForResult(intent, REQUEST_CAMERA);
            } catch (IOException e) {
                Toast.makeText(this, "Error al crear archivo de foto",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File crearArchivoFoto() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String nombreArchivo = "FOTO_" + timestamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(nombreArchivo, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            ivFotoPerfil.setImageURI(fotoUri);
            subirFotoAlServidor();
        }
    }

    private void subirFotoAlServidor() {
        progressBar.setVisibility(View.VISIBLE);
        btnCamara.setEnabled(false);

        executor.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("user_id",
                                String.valueOf(sessionManager.getUserId()))
                        .addFormDataPart("foto", fotoFile.getName(),
                                RequestBody.create(fotoFile,
                                        MediaType.parse("image/jpeg")))
                        .build();

                Request request = new Request.Builder()
                        .url("http://34.140.125.55/gamelog/subir_foto.php")
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    String responseStr = response.body().string();
                    JSONObject json = new JSONObject(responseStr);

                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnCamara.setEnabled(true);
                        try {
                            if (json.getBoolean("success")) {
                                String fotoUrl = json.getString("foto_url");
                                sessionManager.setFoto(fotoUrl);
                                Toast.makeText(this,
                                        "Foto de perfil actualizada",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this,
                                        "Error al subir la foto",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "Error procesando respuesta",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnCamara.setEnabled(true);
                    Toast.makeText(this, "Error de conexión",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void cargarFotoDesdeServidor(String fotoUrl) {
        executor.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://34.140.125.55/gamelog/" + fotoUrl)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    byte[] bytes = response.body().bytes();
                    Bitmap bitmap = android.graphics.BitmapFactory
                            .decodeByteArray(bytes, 0, bytes.length);
                    runOnUiThread(() -> ivFotoPerfil.setImageBitmap(bitmap));
                }
            } catch (Exception e) {
                // Si falla simplemente no carga la foto
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Se necesita permiso de cámara",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}