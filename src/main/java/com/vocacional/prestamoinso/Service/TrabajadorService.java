package com.vocacional.prestamoinso.Service;


import com.vocacional.prestamoinso.Entity.Trabajador;
import com.vocacional.prestamoinso.Repository.TrabajadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Service
public class TrabajadorService {


    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TrabajadorRepository trabajadorRepository;

    public Trabajador login(String username, String password) {
        Trabajador trabajador = trabajadorRepository.findByUsername(username);
        if (trabajador != null && password.equals(trabajador.getPassword()) ) {
            return trabajador;
        }
        return null;
    }

    public void deleteUser(Long id) throws Exception {
        Trabajador trabajador = trabajadorRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con id: " + id));

        trabajadorRepository.delete(trabajador);
    }

    public Trabajador findByUsername(String username) {
        return trabajadorRepository.findByUsername(username);
    }

}
