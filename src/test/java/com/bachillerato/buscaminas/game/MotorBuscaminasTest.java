package com.bachillerato.buscaminas.game;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MotorBuscaminasTest {
    private final MotorBuscaminas motor = new MotorBuscaminas();

    @Test
    void nivelesTienenDimensionesYMinasCorrectas() {
        for (NivelJuego nivel : NivelJuego.values()) {
            MotorBuscaminas.TableroPersistido t = motor.crear(nivel);
            assertEquals(nivel.getFilas(), t.filas);
            assertEquals(nivel.getColumnas(), t.columnas);
            assertEquals(nivel.getMinas(), t.num_minas);
            assertEquals(nivel.getMinas(), t.minas.size());
            assertEquals(nivel.getMinas(), new HashSet<>(t.minas).size());
            assertEquals("EN_CURSO", t.resultado);
        }
    }

    private MotorBuscaminas.TableroPersistido pequeno() {
        MotorBuscaminas.TableroPersistido t = new MotorBuscaminas.TableroPersistido();
        t.filas = 3;
        t.columnas = 3;
        t.num_minas = 1;
        t.minas = List.of(List.of(2,2));
        return t;
    }

    @Test
    void cuentaMinasAdyacentes() {
        var t = pequeno();
        assertEquals(1, motor.contarAdyacentes(t, 1, 1));
        assertEquals(1, motor.contarAdyacentes(t, 1, 2));
        assertEquals(0, motor.contarAdyacentes(t, 0, 0));
    }

    @Test
    void aperturaEnCadenaRevelaTodasLasCasillasSegurasYDaVictoria() {
        var t = pequeno();
        motor.actuar(t, "REVELAR", 0, 0);
        assertEquals(8, t.reveladas.size());
        assertEquals("GANADA", t.resultado);
        assertFalse(t.reveladas.contains(List.of(2,2)));
        assertEquals("OCULTA", motor.vista(t).get(2).get(2).estado());
    }

    @Test
    void explotarMinaDaDerrotaSinFiltrarOtrasMinas() {
        var t = pequeno();
        motor.actuar(t, "REVELAR", 2, 2);
        assertEquals("PERDIDA", t.resultado);
        assertEquals("EXPLOTADA", motor.vista(t).get(2).get(2).estado());
        assertEquals("OCULTA", motor.vista(t).get(0).get(0).estado());
        assertThrows(IllegalArgumentException.class, () -> motor.actuar(t,"BANDERA",0,0));
    }

    @Test
    void alternarBanderaNoAbreCasillas() {
        var t = pequeno();
        motor.actuar(t,"BANDERA",0,0);
        assertEquals(List.of(List.of(0,0)), t.banderas);
        assertEquals("BANDERA", motor.vista(t).get(0).get(0).estado());
        assertThrows(IllegalArgumentException.class, () -> motor.actuar(t,"REVELAR",0,0));
        motor.actuar(t,"BANDERA",0,0);
        assertTrue(t.banderas.isEmpty());
        assertTrue(t.reveladas.isEmpty());
    }

    @Test
    void coordenadasYAccionesInvalidasSeRechazan() {
        var t = pequeno();
        assertThrows(IllegalArgumentException.class, () -> motor.actuar(t,"REVELAR",-1,0));
        assertThrows(IllegalArgumentException.class, () -> motor.actuar(t,"REVELAR",3,0));
        assertThrows(IllegalArgumentException.class, () -> motor.actuar(t,"ACCION_RARA",0,0));
    }

    @Test
    void unaCasillaYaAbiertaNoPermiteRepetirMovimiento() {
        var t = pequeno();
        motor.actuar(t,"REVELAR",0,0);
        assertThrows(IllegalArgumentException.class, () -> motor.actuar(t,"REVELAR",0,0));
    }

    @Test
    void vistaDePartidaNuevaOcultaMinasYNumeros() {
        var t = motor.crear(NivelJuego.EXPERTO);
        var vista = motor.vista(t);
        assertEquals(16,vista.size());
        assertEquals(30,vista.get(0).size());
        for (var fila : vista) {
            for (var celda : fila) {
                assertEquals("OCULTA",celda.estado());
                assertNull(celda.numero());
            }
        }
    }
}
