package com.vocacional.prestamoinso.DTO;


import lombok.Data;

@Data
public class SunatResponseDTO {
    private String razonSocial;
    private String tipoDocumento;
    private String numeroDocumento;
    private String estado;
    private String condicion;
    private String direccion;
    private String distrito;
    private String provincia;
    private String departamento;
}
