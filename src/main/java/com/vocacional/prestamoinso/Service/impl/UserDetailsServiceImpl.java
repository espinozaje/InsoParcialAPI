package com.vocacional.prestamoinso.Service.impl;

import com.vocacional.prestamoinso.Entity.Cliente;
import com.vocacional.prestamoinso.Entity.Trabajador;
import com.vocacional.prestamoinso.Entity.enums.ERole;
import com.vocacional.prestamoinso.Repository.ClienteRepository;
import com.vocacional.prestamoinso.Repository.TrabajadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collection;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Trabajador trabajador = trabajadorRepository.findByUsername(username);
        if (trabajador == null) {
            throw new UsernameNotFoundException("Traabajador no encontrado: " + username);
        }

        return new User(
                trabajador.getUsername(),
                trabajador.getPassword(),
                mapRolesToAuthorities(trabajador.getRole())
        );
    }


    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(ERole role) {
        return role == null ? null :
                java.util.Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }
}
