package com.faculdade.sensor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** O coração do padrão: a cópia é fiel, é de verdade outro objeto e não compartilha estado mutável. */
class ClonagemTest {

    private SensorTemperatura original;
    private SensorPressao pressao;

    @BeforeEach
    void montar() {
        original = new SensorTemperatura("T-MOD", "°C", 80, 90, new Calibracao(1.02, -0.5),
                List.of("linha-envase", "critico"), "K");
        pressao = new SensorPressao("P-MOD", "bar", 8, 10, new Calibracao(1.0, 0.0), List.of("linha-envase"), 16.0);
    }

    // ---- fidelidade ----

    @Test
    void cloneDeveSerOutroObjetoDoMesmoTipoConcreto() {
        SensorIndustrial copia = original.clonar("T-02");

        assertNotSame(original, copia);
        assertEquals(SensorTemperatura.class, copia.getClass());
        assertEquals(SensorPressao.class, pressao.clonar("P-02").getClass());
    }

    @Test
    void cloneDeveCopiarTodaAConfiguracaoMenosOId() {
        SensorTemperatura copia = original.clonar("T-02");

        assertEquals("T-02", copia.id());
        assertEquals("°C", copia.unidade());
        assertEquals(80, copia.limiteAlerta());
        assertEquals(90, copia.limiteCritico());
        assertEquals(1.02, copia.calibracao().ganho());
        assertEquals(-0.5, copia.calibracao().offset());
        assertEquals(List.of("linha-envase", "critico"), copia.tags());
        assertEquals("K", copia.tipoTermopar());
    }

    @Test
    void cloneDeveCopiarOsCamposEspecificosDaSubclasse() {
        SensorPressao copia = pressao.clonar("P-02");

        assertEquals(16.0, copia.faixaMaximaBar());
    }

    @Test
    void cloneSemArgumentoDeveManterOId() {
        SensorIndustrial copia = original.clonar();

        assertEquals("T-MOD", copia.id());
        assertNotSame(original, copia);
    }

    @Test
    void cloneDeveRecusarIdInvalido() {
        assertThrows(NullPointerException.class, () -> original.clonar(null));
        assertThrows(IllegalArgumentException.class, () -> original.clonar(" "));
    }

    // ---- independência (cópia profunda) ----

    @Test
    void reajustarACalibracaoDaCopiaNaoDeveAfetarOOriginal() {
        SensorTemperatura copia = original.clonar("T-02");

        copia.calibracao().ajustar(2.0, 10.0);

        assertEquals(1.02, original.calibracao().ganho());
        assertEquals(-0.5, original.calibracao().offset());
    }

    @Test
    void reajustarACalibracaoDoOriginalNaoDeveAfetarCopiasJaFeitas() {
        SensorTemperatura copia = original.clonar("T-02");

        original.calibracao().ajustar(3.0, 7.0);

        assertEquals(1.02, copia.calibracao().ganho());
        assertEquals(-0.5, copia.calibracao().offset());
    }

    @Test
    void calibracoesDeCopiasDiferentesDevemSerObjetosDiferentes() {
        SensorTemperatura a = original.clonar("A");
        SensorTemperatura b = original.clonar("B");

        assertNotSame(a.calibracao(), b.calibracao());
        assertNotSame(a.calibracao(), original.calibracao());
    }

    @Test
    void tagsDaCopiaEDoOriginalDevemSerIndependentes() {
        SensorTemperatura copia = original.clonar("T-02");

        copia.adicionarTag("so-da-copia");
        original.adicionarTag("so-do-original");

        assertEquals(List.of("linha-envase", "critico", "so-da-copia"), copia.tags());
        assertEquals(List.of("linha-envase", "critico", "so-do-original"), original.tags());
    }

    @Test
    void copiaDeCopiaDeveContinuarIndependente() {
        SensorTemperatura filho = original.clonar("F");
        SensorTemperatura neto = filho.clonar("N");

        neto.calibracao().ajustar(5.0, 5.0);
        neto.adicionarTag("x");

        assertEquals(1.02, filho.calibracao().ganho());
        assertEquals(1.02, original.calibracao().ganho());
        assertEquals(2, filho.tags().size());
    }

    // ---- histórico: decisão explícita de NÃO copiar ----

    @Test
    void copiaDeveNascerSemHistoricoEntretantoOOriginalMantemOSeu() {
        original.registrar(70.0);
        original.registrar(95.0);

        SensorTemperatura copia = original.clonar("T-02");

        assertTrue(copia.historico().isEmpty());
        assertTrue(copia.ultimoValor().isEmpty());
        assertEquals(Nivel.NORMAL, copia.nivel());
        assertEquals(2, original.historico().size());
    }

    @Test
    void leiturasDaCopiaNaoDevemAparecerNoOriginal() {
        SensorTemperatura copia = original.clonar("T-02");

        copia.registrar(100.0);

        assertTrue(original.historico().isEmpty());
        assertEquals(1, copia.historico().size());
    }

    @Test
    void copiaDeveUsarAPropriaCalibracaoNasLeituras() {
        SensorTemperatura copia = original.clonar("T-02");
        copia.calibracao().ajustar(1.0, 0.0);

        original.registrar(100.0);
        copia.registrar(100.0);

        assertEquals(101.5, original.ultimoValor().orElseThrow(), 1e-9);   // 100 × 1,02 − 0,5
        assertEquals(100.0, copia.ultimoValor().orElseThrow(), 1e-9);
    }
}
