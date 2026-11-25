package com.faculdade.sensor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegistroPrototiposTest {

    private RegistroPrototipos catalogo;
    private SensorTemperatura modelo;

    @BeforeEach
    void montar() {
        catalogo = new RegistroPrototipos();
        modelo = new SensorTemperatura("modelo", "°C", 80, 90, new Calibracao(1.0, -0.5),
                List.of("linha-envase"), "K");
        catalogo.registrar("termopar-K", modelo);
        catalogo.registrar("transdutor", new SensorPressao("modelo", "bar", 8, 10,
                new Calibracao(1.0, 0.0), List.of(), 16.0));
    }

    @Test
    void criarDeveDevolverSensorDoTipoDoModeloComONovoId() {
        SensorIndustrial t = catalogo.criar("termopar-K", "T-01");
        SensorIndustrial p = catalogo.criar("transdutor", "P-01");

        assertEquals(SensorTemperatura.class, t.getClass());
        assertEquals("T-01", t.id());
        assertEquals(SensorPressao.class, p.getClass());
        assertEquals("P-01", p.id());
    }

    @Test
    void cadaCriarDeveDevolverUmObjetoNovoEIndependente() {
        SensorIndustrial a = catalogo.criar("termopar-K", "T-01");
        SensorIndustrial b = catalogo.criar("termopar-K", "T-02");

        a.calibracao().ajustar(2.0, 2.0);
        a.adicionarTag("so-do-a");

        assertNotSame(a, b);
        assertEquals(1.0, b.calibracao().ganho());
        assertEquals(List.of("linha-envase"), b.tags());
        // e um terceiro, criado depois, continua com o valor do modelo
        assertEquals(1.0, catalogo.criar("termopar-K", "T-03").calibracao().ganho());
    }

    @Test
    void catalogoDeveGuardarSuaPropriaCopiaDoModelo() {
        // mexer no objeto original depois de registrar NÃO pode alterar o que o catálogo entrega
        modelo.calibracao().ajustar(9.0, 9.0);
        modelo.adicionarTag("alteracao-posterior");

        SensorIndustrial novo = catalogo.criar("termopar-K", "T-01");

        assertEquals(1.0, novo.calibracao().ganho());
        assertEquals(-0.5, novo.calibracao().offset());
        assertEquals(List.of("linha-envase"), novo.tags());
    }

    @Test
    void sensorCriadoNaoDeveSerOModeloGuardado() {
        SensorIndustrial novo = catalogo.criar("termopar-K", "T-01");
        novo.calibracao().ajustar(5.0, 5.0);
        novo.registrar(10.0);

        SensorIndustrial outro = catalogo.criar("termopar-K", "T-02");

        assertEquals(1.0, outro.calibracao().ganho());
        assertTrue(outro.historico().isEmpty());
    }

    @Test
    void modeloRegistradoComHistoricoNaoDeveEntregarCopiasComHistorico() {
        modelo.registrar(99.0);
        RegistroPrototipos outro = new RegistroPrototipos();
        outro.registrar("m", modelo);

        assertTrue(outro.criar("m", "X").historico().isEmpty());
    }

    @Test
    void chaveDesconhecidaDeveSerRecusada() {
        assertThrows(IllegalArgumentException.class, () -> catalogo.criar("nao-existe", "T-01"));
    }

    @Test
    void chaveRepetidaDeveSerRecusadaSemSubstituirOModelo() {
        SensorTemperatura outro = new SensorTemperatura("m", "°C", 10, 20, new Calibracao(5.0, 0.0), List.of(), "J");

        assertThrows(IllegalStateException.class, () -> catalogo.registrar("termopar-K", outro));

        assertEquals("K", ((SensorTemperatura) catalogo.criar("termopar-K", "T-01")).tipoTermopar());
    }

    @Test
    void registroDeveValidarArgumentos() {
        assertThrows(NullPointerException.class, () -> catalogo.registrar(null, modelo));
        assertThrows(NullPointerException.class, () -> catalogo.registrar("x", null));
        assertThrows(IllegalArgumentException.class, () -> catalogo.registrar(" ", modelo));
    }

    @Test
    void chavesDevemVirEmOrdemDeRegistroESomenteLeitura() {
        assertEquals(List.of("termopar-K", "transdutor"), catalogo.chaves());
        assertThrows(UnsupportedOperationException.class, () -> catalogo.chaves().add("x"));
    }
}
