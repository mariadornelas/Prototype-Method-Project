package com.faculdade.sensor;

public class Calibracao {

    private double ganho;
    private double offset;

    public Calibracao(double ganho, double offset) {
        validar(ganho, offset);
        this.ganho = ganho;
        this.offset = offset;
    }

    private static void validar(double ganho, double offset) {
        if (!Double.isFinite(ganho) || !Double.isFinite(offset)) {
            throw new IllegalArgumentException("ganho e offset devem ser finitos");
        }
        if (ganho <= 0) {
            throw new IllegalArgumentException("o ganho deve ser positivo");
        }
    }

    public double ganho() {
        return ganho;
    }

    public double offset() {
        return offset;
    }

    public void ajustar(double novoGanho, double novoOffset) {
        validar(novoGanho, novoOffset);
        this.ganho = novoGanho;
        this.offset = novoOffset;
    }

    public double aplicar(double bruto) {
        return bruto * ganho + offset;
    }

    public Calibracao copiar() {
        return new Calibracao(ganho, offset);
    }
}
