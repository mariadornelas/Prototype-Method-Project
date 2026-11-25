package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** O que caracteriza o Prototype: clonar sem conhecer a classe concreta, sem Cloneable. */
class PrototypeDesignTest {

    /** Cliente que só conhece o tipo base: não sabe se é termopar ou transdutor. */
    private static List<SensorIndustrial> produzir(SensorIndustrial modelo, int quantidade) {
        List<SensorIndustrial> lote = new ArrayList<>();
        for (int i = 1; i <= quantidade; i++) {
            lote.add(modelo.clonar(modelo.id() + "-" + i));
        }
        return lote;
    }

    @Test
    void clienteDeveClonarSemConhecerAClasseConcreta() {
        SensorIndustrial termopar = new SensorTemperatura("T", "°C", 80, 90, new Calibracao(1, 0), List.of(), "K");
        SensorIndustrial transdutor = new SensorPressao("P", "bar", 8, 10, new Calibracao(1, 0), List.of(), 16);

        List<SensorIndustrial> temperaturas = produzir(termopar, 3);
        List<SensorIndustrial> pressoes = produzir(transdutor, 2);

        assertEquals(3, temperaturas.size());
        assertTrue(temperaturas.stream().allMatch(s -> s instanceof SensorTemperatura));
        assertTrue(pressoes.stream().allMatch(s -> s instanceof SensorPressao));
        assertEquals("T-3", temperaturas.get(2).id());
    }

    @Test
    void loteClonadoNaoDeveCompartilharEstadoEntreSi() {
        SensorIndustrial modelo = new SensorTemperatura("T", "°C", 80, 90, new Calibracao(1, 0), List.of("a"), "K");
        List<SensorIndustrial> lote = produzir(modelo, 3);

        lote.get(0).calibracao().ajustar(7, 7);
        lote.get(0).adicionarTag("b");

        for (int i = 1; i < 3; i++) {
            assertEquals(1.0, lote.get(i).calibracao().ganho());
            assertEquals(List.of("a"), lote.get(i).tags());
        }
        assertEquals(1.0, modelo.calibracao().ganho());
    }

    @Test
    void naoDeveUsarObjectCloneNemCloneable() {
        assertFalse(Cloneable.class.isAssignableFrom(SensorIndustrial.class));
        assertFalse(Cloneable.class.isAssignableFrom(Calibracao.class));
    }

    @Test
    void cadaSubclasseConcretaDeveImplementarSuaPropriaClonagem() throws Exception {
        for (Class<?> c : List.of(SensorTemperatura.class, SensorPressao.class)) {
            Method m = c.getDeclaredMethod("clonar", String.class);
            assertFalse(Modifier.isAbstract(m.getModifiers()), c.getSimpleName());
            assertEquals(c, m.getReturnType(), "retorno covariante em " + c.getSimpleName());
        }
    }

    @Test
    void construtoresDeCopiaNaoDevemSerPublicos() {
        // a cópia só acontece por clonar(); ninguém de fora monta uma cópia "pela metade"
        for (Class<?> c : List.of(SensorTemperatura.class, SensorPressao.class)) {
            long publicosDeCopia = java.util.Arrays.stream(c.getDeclaredConstructors())
                    .filter(k -> Modifier.isPublic(k.getModifiers()))
                    .filter(k -> k.getParameterCount() == 2 && k.getParameterTypes()[0] == c)
                    .count();
            assertEquals(0, publicosDeCopia, c.getSimpleName());
        }
    }

    @Test
    void sensorBaseDeveSerAbstrato() {
        assertTrue(Modifier.isAbstract(SensorIndustrial.class.getModifiers()));
    }
}
