package com.vocacional.prestamoinso.DTO;


import com.vocacional.prestamoinso.Entity.Cliente;

import java.util.List;

public class PrestamoDTO {
    private Long id;
        private String dni;
        private ClienteDTO cliente;
        private double monto;
        private int plazo;
        private double interes;
        private List<CronogramaPagosDTO> cronogramaPagos;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    public List<CronogramaPagosDTO> getCronogramaPagos() {
        return cronogramaPagos;
    }

    public void setCronogramaPagos(List<CronogramaPagosDTO> cronogramaPagos) {
        this.cronogramaPagos = cronogramaPagos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

        public double getMonto() {
            return monto;
        }

        public void setMonto(double monto) {
            this.monto = monto;
        }

        public int getPlazo() {
            return plazo;
        }

        public void setPlazo(int plazo) {
            this.plazo = plazo;
        }

        public double getInteres() {
            return interes;
        }

        public void setInteres(double interes) {
            this.interes = interes;
        }
    }