package com.bachillerato.buscaminas.game;

import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/** Reglas completas del juego. Nunca se entrega TableroPersistido al navegador. */
public class MotorBuscaminas {
    private final SecureRandom aleatorio = new SecureRandom();

    /** Se serializa exclusivamente en la columna JSON de MySQL. */
    public static class TableroPersistido {
        public int filas;
        public int columnas;
        public int num_minas;
        public long inicioEpochMs;
        public String resultado = "EN_CURSO";
        public List<List<Integer>> minas = new ArrayList<>();
        public List<List<Integer>> reveladas = new ArrayList<>();
        public List<List<Integer>> banderas = new ArrayList<>();
        public List<Integer> minaExplotada;

        public TableroPersistido() { }
    }

    public record CasillaVisible(String estado, Integer numero) { }
    private record Pos(int fila, int columna) { }

    public TableroPersistido crear(NivelJuego nivel) {
        TableroPersistido tablero = new TableroPersistido();
        tablero.filas = nivel.getFilas();
        tablero.columnas = nivel.getColumnas();
        tablero.num_minas = nivel.getMinas();
        tablero.inicioEpochMs = System.currentTimeMillis();

        List<Pos> disponibles = new ArrayList<>();
        for (int fila = 0; fila < tablero.filas; fila++) {
            for (int columna = 0; columna < tablero.columnas; columna++) {
                disponibles.add(new Pos(fila, columna));
            }
        }
        Collections.shuffle(disponibles, aleatorio);
        for (int i = 0; i < tablero.num_minas; i++) {
            Pos pos = disponibles.get(i);
            tablero.minas.add(List.of(pos.fila(), pos.columna()));
        }
        return tablero;
    }

    public void actuar(TableroPersistido tablero, String accion, int fila, int columna) {
        if (!"EN_CURSO".equals(tablero.resultado)) {
            throw new IllegalArgumentException("La partida ya finalizó");
        }
        comprobarCoordenadas(tablero, fila, columna);
        Pos posicion = new Pos(fila, columna);
        Set<Pos> abiertas = posiciones(tablero.reveladas);
        Set<Pos> marcadas = posiciones(tablero.banderas);

        if ("BANDERA".equals(accion)) {
            if (abiertas.contains(posicion)) {
                throw new IllegalArgumentException("No se puede marcar una casilla revelada");
            }
            if (!marcadas.add(posicion)) {
                marcadas.remove(posicion);
            }
            tablero.banderas = serializar(marcadas);
        } else if ("REVELAR".equals(accion)) {
            if (marcadas.contains(posicion)) {
                throw new IllegalArgumentException("Quite la bandera antes de revelar la casilla");
            }
            if (abiertas.contains(posicion)) {
                throw new IllegalArgumentException("La casilla ya estaba revelada");
            }
            Set<Pos> minas = posiciones(tablero.minas);
            if (minas.contains(posicion)) {
                tablero.minaExplotada = List.of(fila, columna);
                tablero.resultado = "PERDIDA";
            } else {
                expandir(tablero, posicion, minas, marcadas, abiertas);
                tablero.reveladas = serializar(abiertas);
                if (abiertas.size() == tablero.filas * tablero.columnas - tablero.num_minas) {
                    tablero.resultado = "GANADA";
                }
            }
        } else {
            throw new IllegalArgumentException("Acción inválida: use REVELAR o BANDERA");
        }
    }

    private void expandir(TableroPersistido tablero, Pos inicio, Set<Pos> minas,
                          Set<Pos> marcadas, Set<Pos> abiertas) {
        Queue<Pos> pendientes = new ArrayDeque<>();
        pendientes.add(inicio);
        while (!pendientes.isEmpty()) {
            Pos pos = pendientes.remove();
            if (minas.contains(pos) || marcadas.contains(pos) || !abiertas.add(pos)) {
                continue;
            }
            if (contarAdyacentes(tablero, pos.fila(), pos.columna(), minas) == 0) {
                for (int df = -1; df <= 1; df++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        Pos vecina = new Pos(pos.fila() + df, pos.columna() + dc);
                        if (enRango(tablero, vecina) && !abiertas.contains(vecina)
                                && !marcadas.contains(vecina) && !minas.contains(vecina)) {
                            pendientes.add(vecina);
                        }
                    }
                }
            }
        }
    }

    public int contarAdyacentes(TableroPersistido tablero, int fila, int columna) {
        comprobarCoordenadas(tablero, fila, columna);
        return contarAdyacentes(tablero, fila, columna, posiciones(tablero.minas));
    }

    private int contarAdyacentes(TableroPersistido tablero, int fila, int columna, Set<Pos> minas) {
        int total = 0;
        for (int df = -1; df <= 1; df++) {
            for (int dc = -1; dc <= 1; dc++) {
                if ((df != 0 || dc != 0) && minas.contains(new Pos(fila + df, columna + dc))) {
                    total++;
                }
            }
        }
        return total;
    }

    /** Solo revela números de casillas abiertas; jamás devuelve la lista interna de minas. */
    public List<List<CasillaVisible>> vista(TableroPersistido tablero) {
        Set<Pos> abiertas = posiciones(tablero.reveladas);
        Set<Pos> banderas = posiciones(tablero.banderas);
        Set<Pos> minas = posiciones(tablero.minas);
        List<List<CasillaVisible>> resultado = new ArrayList<>();
        for (int f = 0; f < tablero.filas; f++) {
            List<CasillaVisible> fila = new ArrayList<>();
            for (int c = 0; c < tablero.columnas; c++) {
                Pos posicion = new Pos(f, c);
                if (abiertas.contains(posicion)) {
                    fila.add(new CasillaVisible("ABIERTA", contarAdyacentes(tablero, f, c, minas)));
                } else if (tablero.minaExplotada != null
                        && tablero.minaExplotada.equals(List.of(f, c))) {
                    fila.add(new CasillaVisible("EXPLOTADA", null));
                } else if (banderas.contains(posicion)) {
                    fila.add(new CasillaVisible("BANDERA", null));
                } else {
                    fila.add(new CasillaVisible("OCULTA", null));
                }
            }
            resultado.add(fila);
        }
        return resultado;
    }

    private void comprobarCoordenadas(TableroPersistido tablero, int fila, int columna) {
        if (fila < 0 || columna < 0 || fila >= tablero.filas || columna >= tablero.columnas) {
            throw new IllegalArgumentException("Coordenadas fuera del tablero");
        }
    }

    private boolean enRango(TableroPersistido tablero, Pos pos) {
        return pos.fila() >= 0 && pos.columna() >= 0
                && pos.fila() < tablero.filas && pos.columna() < tablero.columnas;
    }

    private Set<Pos> posiciones(List<List<Integer>> pares) {
        Set<Pos> resultado = new HashSet<>();
        if (pares != null) {
            for (List<Integer> par : pares) {
                if (par != null && par.size() == 2) {
                    resultado.add(new Pos(par.get(0), par.get(1)));
                }
            }
        }
        return resultado;
    }

    private List<List<Integer>> serializar(Set<Pos> posiciones) {
        return posiciones.stream().sorted((a,b) -> {
            int x = Integer.compare(a.fila(), b.fila());
            return x != 0 ? x : Integer.compare(a.columna(), b.columna());
        }).map(p -> List.of(p.fila(), p.columna())).toList();
    }
}
