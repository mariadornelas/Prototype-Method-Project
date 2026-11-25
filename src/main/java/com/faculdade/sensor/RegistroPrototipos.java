package com.faculdade.sensor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Objects;

public class RegistroPrototipos {

    private final Map<String, SensorIndustrial> modelos = new LinkedHashMap<>();

    public void registrar(String chave, SensorIndustrial modelo) {
        Objects.requireNonNull(chave, "chave não pode ser nula");
        Objects.requireNonNull(modelo, "modelo não pode ser nulo");
        if (chave.isBlank()) {
            throw new IllegalArgumentException("chave não pode ser vazia");
        }
        if (modelos.containsKey(chave)) {
            throw new IllegalStateException("já existe um modelo com a chave " + chave);
        }
        modelos.put(chave, modelo.clonar());
    }

    public SensorIndustrial criar(String chave, String novoId) {
        SensorIndustrial modelo = modelos.get(chave);
        if (modelo == null) {
            throw new IllegalArgumentException("modelo não cadastrado: " + chave);
        }
        return modelo.clonar(novoId);
    }

    public List<String> chaves() {
        return Collections.unmodifiableList(new ArrayList<>(modelos.keySet()));
    }
}
