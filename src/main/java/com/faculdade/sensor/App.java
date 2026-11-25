package com.faculdade.sensor;

import java.util.List;
import java.util.Locale;

public class App {

    public static void main(String[] args) {
        RegistroPrototipos catalogo = new RegistroPrototipos();

        catalogo.registrar("termopar-K", new SensorTemperatura("modelo", "°C", 80, 90,
                new Calibracao(1.0, -0.5), List.of("linha-envase", "processo-critico"), "K"));
        catalogo.registrar("transdutor-10bar", new SensorPressao("modelo", "bar", 8, 10,
                new Calibracao(1.0, 0.0), List.of("linha-envase"), 16.0));

        System.out.println("=== Três termopares e um transdutor, a partir dos modelos ===");
        SensorIndustrial t01 = catalogo.criar("termopar-K", "T-01");
        SensorIndustrial t02 = catalogo.criar("termopar-K", "T-02");
        SensorIndustrial t03 = catalogo.criar("termopar-K", "T-03");
        SensorIndustrial p01 = catalogo.criar("transdutor-10bar", "P-01");

        System.out.println("=== T-02 foi recalibrado e ganhou uma tag: os outros NÃO mudam (cópia profunda) ===");
        t02.calibracao().ajustar(1.02, -1.0);
        t02.adicionarTag("recalibrado-2026-11");

        for (SensorIndustrial s : List.of(t01, t02, t03)) {
            s.registrar(80.0);
        }
        p01.registrar(9.0);

        for (SensorIndustrial s : List.of(t01, t02, t03, p01)) {
            System.out.printf(Locale.ROOT, "  %-5s %-17s ganho %.2f offset %5.2f  leitura %6.2f %-3s %-7s tags %s%n",
                    s.id(), s.getClass().getSimpleName(), s.calibracao().ganho(), s.calibracao().offset(),
                    s.ultimoValor().orElseThrow(), s.unidade(), s.nivel(), s.tags());
        }

        System.out.println();
        System.out.println("=== Uma cópia nasce sem histórico, mesmo que o original tenha leituras ===");
        SensorIndustrial copia = t01.clonar("T-01-reserva");
        System.out.println("  T-01 tem " + t01.historico().size() + " leitura(s); a cópia tem " + copia.historico().size());

        System.out.println();
        System.out.println("=== O catálogo guarda sua própria cópia do modelo ===");
        System.out.println("  chaves: " + catalogo.chaves());
    }
}
