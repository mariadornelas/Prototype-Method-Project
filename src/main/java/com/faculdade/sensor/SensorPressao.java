package com.faculdade.sensor;

import java.util.Collection;

public class SensorPressao extends SensorIndustrial {

    private final double faixaMaximaBar;

    public SensorPressao(String id, String unidade, double limiteAlerta, double limiteCritico,
                         Calibracao calibracao, Collection<String> tags, double faixaMaximaBar) {
        super(id, unidade, limiteAlerta, limiteCritico, calibracao, tags);
        if (!Double.isFinite(faixaMaximaBar) || faixaMaximaBar <= 0) {
            throw new IllegalArgumentException("a faixa máxima deve ser positiva");
        }
        this.faixaMaximaBar = faixaMaximaBar;
    }

    private SensorPressao(SensorPressao original, String novoId) {
        super(original, novoId);
        this.faixaMaximaBar = original.faixaMaximaBar;
    }

    @Override
    public SensorPressao clonar(String novoId) {
        return new SensorPressao(this, novoId);
    }

    public double faixaMaximaBar() {
        return faixaMaximaBar;
    }
}
