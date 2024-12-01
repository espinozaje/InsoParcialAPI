package com.vocacional.prestamoinso.Service;


import com.vocacional.prestamoinso.Entity.Trabajador;
import com.vocacional.prestamoinso.Repository.TrabajadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
public class TrabajadorService {


    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TrabajadorRepository trabajadorRepository;



    public void deleteUser(Long id) throws Exception {
        Trabajador trabajador = trabajadorRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con id: " + id));

        trabajadorRepository.delete(trabajador);
    }



}
