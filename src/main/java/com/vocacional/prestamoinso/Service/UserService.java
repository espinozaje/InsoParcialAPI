package com.vocacional.prestamoinso.Service;


import com.vocacional.prestamoinso.DTO.TrabajadorDTO;
import com.vocacional.prestamoinso.DTO.UserDTO;
import com.vocacional.prestamoinso.Entity.Trabajador;
import com.vocacional.prestamoinso.Entity.User;
import com.vocacional.prestamoinso.Entity.enums.ERole;
import com.vocacional.prestamoinso.Mapper.TrabajadorMappper;
import com.vocacional.prestamoinso.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {


    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TrabajadorMappper trabajadorMappper;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, TrabajadorMappper trabajadorMappper) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.trabajadorMappper = trabajadorMappper;
    }

    public TrabajadorDTO registerTrabajador(TrabajadorDTO trabajadorDTO) {
        Trabajador trabajador = trabajadorMappper.toEntity(trabajadorDTO);
        ERole eRole = ERole.WORKER;
        trabajador.setNombre(trabajadorDTO.getNombre());
        trabajador.setApellido(trabajadorDTO.getApellido());
        trabajador.setUsername(trabajadorDTO.getUsername());
        trabajador.setEmail(trabajadorDTO.getEmail());
        trabajador.setPassword(passwordEncoder.encode(trabajadorDTO.getPassword()));
        trabajador.setRole(eRole);
        trabajador = userRepository.save(trabajador);
        return trabajadorMappper.toDTO(trabajador);
    }


    public TrabajadorDTO registerAdmin(TrabajadorDTO trabajadorDTO) {
        Trabajador trabajador = trabajadorMappper.toEntity(trabajadorDTO);
        ERole eRole = ERole.ADMIN;
        trabajador.setNombre(trabajadorDTO.getNombre());
        trabajador.setApellido(trabajadorDTO.getApellido());
        trabajador.setUsername(trabajadorDTO.getUsername());
        trabajador.setEmail(trabajadorDTO.getEmail());
        trabajador.setPassword(passwordEncoder.encode(trabajadorDTO.getPassword()));
        trabajador.setRole(eRole);
        trabajador = userRepository.save(trabajador);
        return trabajadorMappper.toDTO(trabajador);
    }
}
