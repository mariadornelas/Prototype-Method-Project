package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Comportamento comum do sensor e da calibração, sem falar de clonagem. */
class SensorIndustrialTest {

    private SensorTemperatura sensor() {
        return new SensorTemperatura("T-01", "°C", 80, 90, new Calibracao(1.0, -0.5), List.of("a"), "K");
    }

    // ---- Calibracao ----

    @Test
    void calibracaoDeveAplicarGanhoEOffset() {
        assertEquals(21.0, new Calibracao(2.0, 1.0).aplicar(10.0));
        assertEquals(9.5, new Calibracao(1.0, -0.5).aplicar(10.0));
    }

    @Test
    void calibracaoDeveValidarEPoderSerReajustada() {
        assertThrows(IllegalArgumentException.class, () -> new Calibracao(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Calibracao(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Calibracao(1, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new Calibracao(Double.POSITIVE_INFINITY, 0));

        Calibracao c = new Calibracao(1.0, 0.0);
        c.ajustar(2.0, 3.0);
        assertEquals(2.0, c.ganho());
        assertEquals(3.0, c.offset());
        assertThrows(IllegalArgumentException.class, () -> c.ajustar(0, 1));
        assertEquals(2.0, c.ganho());   // ajuste inválido não altera nada
    }

    @Test
    void copiaDaCalibracaoDeveSerIndependente() {
        Calibracao a = new Calibracao(1.0, 0.0);
        Calibracao b = a.copiar();

        b.ajustar(5.0, 5.0);

        assertEquals(1.0, a.ganho());
        assertEquals(0.0, a.offset());
    }

    // ---- leituras e nível ----

    @Test
    void leituraDeveSerCalibradaEGuardadaEmOrdem() {
        SensorTemperatura s = sensor();

        s.registrar(70.0);
        s.registrar(85.5);

        assertEquals(List.of(69.5, 85.0), s.historico());
        assertEquals(85.0, s.ultimoValor().orElseThrow());
    }

    @Test
    void semLeituraDeveSerNormalESemUltimoValor() {
        SensorTemperatura s = sensor();

        assertEquals(Nivel.NORMAL, s.nivel());
        assertTrue(s.ultimoValor().isEmpty());
    }

    @Test
    void nivelDeveUsarLimitesInclusivosSobreOValorCalibrado() {
        SensorTemperatura s = sensor();   // offset -0,5

        s.registrar(80.4);   // 79,9
        assertEquals(Nivel.NORMAL, s.nivel());
        s.registrar(80.5);   // 80,0
        assertEquals(Nivel.ALERTA, s.nivel());
        s.registrar(90.4);   // 89,9
        assertEquals(Nivel.ALERTA, s.nivel());
        s.registrar(90.5);   // 90,0
        assertEquals(Nivel.CRITICO, s.nivel());
    }

    @Test
    void leituraNaoNumericaDeveSerRecusadaSemAlterarOHistorico() {
        SensorTemperatura s = sensor();
        s.registrar(50.0);

        assertThrows(IllegalArgumentException.class, () -> s.registrar(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> s.registrar(Double.NEGATIVE_INFINITY));

        assertEquals(1, s.historico().size());
    }

    @Test
    void historicoETagsDevemSerSomenteLeitura() {
        SensorTemperatura s = sensor();
        s.registrar(1.0);

        assertThrows(UnsupportedOperationException.class, () -> s.historico().clear());
        assertThrows(UnsupportedOperationException.class, () -> s.tags().add("x"));
    }

    // ---- construção ----

    @Test
    void construcaoDeveValidarCampos() {
        Calibracao c = new Calibracao(1, 0);
        assertThrows(NullPointerException.class, () -> new SensorTemperatura(null, "u", 1, 2, c, List.of(), "K"));
        assertThrows(IllegalArgumentException.class, () -> new SensorTemperatura(" ", "u", 1, 2, c, List.of(), "K"));
        assertThrows(NullPointerException.class, () -> new SensorTemperatura("T", null, 1, 2, c, List.of(), "K"));
        assertThrows(NullPointerException.class, () -> new SensorTemperatura("T", "u", 1, 2, null, List.of(), "K"));
        assertThrows(NullPointerException.class, () -> new SensorTemperatura("T", "u", 1, 2, c, null, "K"));
        assertThrows(IllegalArgumentException.class, () -> new SensorTemperatura("T", "u", 2, 2, c, List.of(), "K"));
        assertThrows(IllegalArgumentException.class, () -> new SensorTemperatura("T", "u", Double.NaN, 2, c, List.of(), "K"));
        assertThrows(NullPointerException.class, () -> new SensorTemperatura("T", "u", 1, 2, c, List.of(), null));
        assertThrows(IllegalArgumentException.class, () -> new SensorPressao("P", "bar", 1, 2, c, List.of(), 0));
    }

    @Test
    void construtorNaoDeveGuardarAsReferenciasRecebidas() {
        Calibracao c = new Calibracao(1.0, 0.0);
        List<String> tags = new ArrayList<>(List.of("a"));
        SensorTemperatura s = new SensorTemperatura("T", "u", 1, 2, c, tags, "K");

        c.ajustar(9.0, 9.0);
        tags.add("b");

        assertEquals(1.0, s.calibracao().ganho());
        assertEquals(List.of("a"), s.tags());
    }

    @Test
    void tagDeveSerValidada() {
        SensorTemperatura s = sensor();

        assertThrows(NullPointerException.class, () -> s.adicionarTag(null));
        assertThrows(IllegalArgumentException.class, () -> s.adicionarTag(" "));
        s.adicionarTag("nova");
        assertEquals(List.of("a", "nova"), s.tags());
    }
}
