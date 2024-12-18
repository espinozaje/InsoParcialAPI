package com.vocacional.prestamoinso.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Prestamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    private String nroDocumento;
    private double monto;
    private double interes;
    private int plazo; // En meses (ejemplo: 6 meses, 12 meses)

    @OneToMany(mappedBy = "prestamo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CronogramaPagos> cronogramaPagos;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Lima")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private String estado;
}
