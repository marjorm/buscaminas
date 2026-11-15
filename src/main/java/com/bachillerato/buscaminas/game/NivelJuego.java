package com.bachillerato.buscaminas.game;

public enum NivelJuego {
    PRINCIPIANTE(9, 9, 10),
    INTERMEDIO(16, 16, 40),
    EXPERTO(16, 30, 99);

    private final int filas;
    private final int columnas;
    private final int minas;

    NivelJuego(int filas, int columnas, int minas) {
        this.filas = filas;
        this.columnas = columnas;
        this.minas = minas;
    }

    public int getFilas() { return filas; }
    public int getColumnas() { return columnas; }
    public int getMinas() { return minas; }
}
