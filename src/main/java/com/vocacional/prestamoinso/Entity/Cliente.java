package com.vocacional.prestamoinso.Entity;

import com.vocacional.prestamoinso.Entity.enums.ERole;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createAt;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nroDocumento;
    private String tipoPersona;
    private String nacionalidad;
    private ERole role;
    private String estado;
    private String condicion;
    private String direccion;
    private String distrito;
    private String provincia;
    private String departamento;


}
