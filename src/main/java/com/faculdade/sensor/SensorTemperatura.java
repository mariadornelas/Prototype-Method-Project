package com.faculdade.sensor;

import java.util.Collection;
import java.util.Objects;

public class SensorTemperatura extends SensorIndustrial {

    private final String tipoTermopar;

    public SensorTemperatura(String id, String unidade, double limiteAlerta, double limiteCritico,
                             Calibracao calibracao, Collection<String> tags, String tipoTermopar) {
        super(id, unidade, limiteAlerta, limiteCritico, calibracao, tags);
        this.tipoTermopar = Objects.requireNonNull(tipoTermopar, "tipo de termopar não pode ser nulo");
    }

    private SensorTemperatura(SensorTemperatura original, String novoId) {
        super(original, novoId);
        this.tipoTermopar = original.tipoTermopar;
    }

    @Override
    public SensorTemperatura clonar(String novoId) {
        return new SensorTemperatura(this, novoId);
    }

    public String tipoTermopar() {
        return tipoTermopar;
    }
}
