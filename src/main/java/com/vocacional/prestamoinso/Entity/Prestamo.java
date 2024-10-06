package com.vocacional.prestamoinso.Entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Prestamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    private String dni;
    private double monto;
    private double interes;
    private int plazo; // En meses (ejemplo: 6 meses, 12 meses)

    @OneToMany(mappedBy = "prestamo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CronogramaPagos> cronogramaPagos;

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public double getInteres() {
        return interes;
    }

    public void setInteres(double interes) {
        this.interes = interes;
    }

    public int getPlazo() {
        return plazo;
    }

    public void setPlazo(int plazo) {
        this.plazo = plazo;
    }

    public List<CronogramaPagos> getCronogramaPagos() {
        return cronogramaPagos;
    }

    public void setCronogramaPagos(List<CronogramaPagos> cronogramaPagos) {
        this.cronogramaPagos = cronogramaPagos;
    }
}
