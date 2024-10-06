package com.vocacional.prestamoinso.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ClienteDTO {
    @NotNull
    @Pattern(regexp = "\\d{8}", message = "DNI debe tener 8 dígitos")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombres;
    @NotBlank(message = "El apellido es obligatorio")
    private String apellidoPaterno;
    @NotBlank(message = "El apellido es obligatorio")
    private String apellidoMaterno;

    private String nacionalidad;


    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public @NotNull @Pattern(regexp = "\\d{8}", message = "DNI debe tener 8 dígitos") String getDni() {
        return dni;
    }

    public void setDni(@NotNull @Pattern(regexp = "\\d{8}", message = "DNI debe tener 8 dígitos") String dni) {
        this.dni = dni;
    }

    public @NotBlank(message = "El nombre es obligatorio") String getNombres() {
        return nombres;
    }

    public void setNombres(@NotBlank(message = "El nombre es obligatorio") String nombres) {
        this.nombres = nombres;
    }

    public @NotBlank(message = "El apellido es obligatorio") String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(@NotBlank(message = "El apellido es obligatorio") String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public @NotBlank(message = "El apellido es obligatorio") String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(@NotBlank(message = "El apellido es obligatorio") String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }
}
