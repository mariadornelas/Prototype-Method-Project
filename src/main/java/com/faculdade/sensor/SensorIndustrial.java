package com.faculdade.sensor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class SensorIndustrial {

    private final String id;
    private final String unidade;
    private final double limiteAlerta;
    private final double limiteCritico;
    private final Calibracao calibracao;
    private final List<String> tags;
    private final List<Double> historico = new ArrayList<>();   // nunca é copiado

    protected SensorIndustrial(String id, String unidade, double limiteAlerta, double limiteCritico,
                               Calibracao calibracao, Collection<String> tags) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id não pode ser vazio");
        }
        Objects.requireNonNull(unidade, "unidade não pode ser nula");
        Objects.requireNonNull(calibracao, "calibração não pode ser nula");
        Objects.requireNonNull(tags, "tags não pode ser nulo");
        if (!Double.isFinite(limiteAlerta) || !Double.isFinite(limiteCritico)) {
            throw new IllegalArgumentException("limites devem ser finitos");
        }
        if (!(limiteAlerta < limiteCritico)) {
            throw new IllegalArgumentException("o limite de alerta deve ser menor que o crítico");
        }
        this.id = id;
        this.unidade = unidade;
        this.limiteAlerta = limiteAlerta;
        this.limiteCritico = limiteCritico;
        this.calibracao = calibracao.copiar();
        this.tags = new ArrayList<>(tags);
    }

    protected SensorIndustrial(SensorIndustrial original, String novoId) {
        this(novoId, original.unidade, original.limiteAlerta, original.limiteCritico,
                original.calibracao, original.tags);
    }

    public abstract SensorIndustrial clonar(String novoId);

    public SensorIndustrial clonar() {
        return clonar(id);
    }

    public void registrar(double bruto) {
        if (!Double.isFinite(bruto)) {
            throw new IllegalArgumentException("leitura deve ser um número finito: " + bruto);
        }
        historico.add(calibracao.aplicar(bruto));
    }

    public Optional<Double> ultimoValor() {
        return historico.isEmpty() ? Optional.empty() : Optional.of(historico.get(historico.size() - 1));
    }

    public List<Double> historico() {
        return Collections.unmodifiableList(historico);
    }

    public Nivel nivel() {
        if (historico.isEmpty()) return Nivel.NORMAL;
        double v = historico.get(historico.size() - 1);
        if (v >= limiteCritico) return Nivel.CRITICO;
        if (v >= limiteAlerta) return Nivel.ALERTA;
        return Nivel.NORMAL;
    }

    public void adicionarTag(String tag) {
        Objects.requireNonNull(tag, "tag não pode ser nula");
        if (tag.isBlank()) {
            throw new IllegalArgumentException("tag não pode ser vazia");
        }
        tags.add(tag);
    }

    public List<String> tags() {
        return Collections.unmodifiableList(tags);
    }

    public Calibracao calibracao() {
        return calibracao;
    }

    public String id() {
        return id;
    }

    public String unidade() {
        return unidade;
    }

    public double limiteAlerta() {
        return limiteAlerta;
    }

    public double limiteCritico() {
        return limiteCritico;
    }
}
