package com.vocacional.prestamoinso.DTO;

import com.vocacional.prestamoinso.Entity.enums.ERole;
import lombok.Data;

@Data
public class UserDTO {

    private String nombre;
    private String apellido;
    private String username;
    private String password;
    private ERole role;
}
