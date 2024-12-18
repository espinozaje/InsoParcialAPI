package com.vocacional.prestamoinso.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class CronogramaPagos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "prestamo_id")
    private Prestamo prestamo;
    private String estado;
    private LocalDate fechaPago;
    private double montoCuota;
    private double pagoIntereses;  // Nueva columna: pago de intereses
    private double amortizacion;   // Nueva columna: amortización
    private double saldoRestante;
}
