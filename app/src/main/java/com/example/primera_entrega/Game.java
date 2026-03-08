package com.example.primera_entrega;

public class Game {
    private int id;
    private String nombre;
    private String plataforma;
    private String estado; // "Jugando", "Completado", "Abandonado"
    private float horasJugadas;
    private int puntuacion; // 1-10
    private String notas;
    private String fechaUltimaSesion;

    public Game() {}

    public Game(int id, String nombre, String plataforma, String estado,
                float horasJugadas, int puntuacion, String notas, String fechaUltimaSesion) {
        this.id = id;
        this.nombre = nombre;
        this.plataforma = plataforma;
        this.estado = estado;
        this.horasJugadas = horasJugadas;
        this.puntuacion = puntuacion;
        this.notas = notas;
        this.fechaUltimaSesion = fechaUltimaSesion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPlataforma() { return plataforma; }
    public void setPlataforma(String plataforma) { this.plataforma = plataforma; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public float getHorasJugadas() { return horasJugadas; }
    public void setHorasJugadas(float horasJugadas) { this.horasJugadas = horasJugadas; }

    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public String getFechaUltimaSesion() { return fechaUltimaSesion; }
    public void setFechaUltimaSesion(String fechaUltimaSesion) { this.fechaUltimaSesion = fechaUltimaSesion; }
}