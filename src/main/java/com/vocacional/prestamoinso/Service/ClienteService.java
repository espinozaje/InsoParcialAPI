package com.vocacional.prestamoinso.Service;


import com.vocacional.prestamoinso.DTO.ClienteDTO;
import com.vocacional.prestamoinso.DTO.ReniecResponseDTO;
import com.vocacional.prestamoinso.Entity.Cliente;
import com.vocacional.prestamoinso.Entity.enums.ERole;
import com.vocacional.prestamoinso.Repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;


@Service
public class ClienteService {

    private final String apiUrl = "https://api.apis.net.pe/v2/reniec/dni?numero={dni}";
    private final String token = "apis-token-10790.r2YzveVxIjOD5d2EFuGrst1uQFnquzDJ"; // Reemplaza con tu token
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClienteRepository clienteRepository;

    public Cliente registrarCliente(ClienteDTO registroClienteDTO) {
        ReniecResponseDTO datosReniec = validarDNI(registroClienteDTO.getDni());

        if (datosReniec == null) {
            throw new RuntimeException("DNI no válido o no encontrado en RENIEC");
        }


        Cliente cliente = new Cliente();
        cliente.setDni(registroClienteDTO.getDni());
        cliente.setNombres(datosReniec.getNombres());
        cliente.setApellidoPaterno(datosReniec.getApellidoPaterno());
        cliente.setApellidoMaterno(datosReniec.getApellidoMaterno());
        cliente.setLocalDateTime(LocalDateTime.now());
        cliente.setNacionalidad("PERUANO");
        cliente.setRole(ERole.USER);

        return clienteRepository.save(cliente);
    }

    public Cliente login(String dni, String password) {
        Cliente cliente = clienteRepository.findByDni(dni);
        if (cliente != null && passwordEncoder.matches(password, cliente.getPassword())) {
            return cliente;
        }
        return null;
    }


    public ReniecResponseDTO validarDNI(String dni) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Realiza la llamada a la API de RENIEC
            ResponseEntity<ReniecResponseDTO> response = restTemplate.exchange(
                    apiUrl, HttpMethod.GET, entity, ReniecResponseDTO.class, dni
            );

            return response.getBody(); // Devuelve la respuesta si es válida
        } catch (Exception e) {
            // Si hay algún error o el DNI no es válido, captura la excepción y retorna null
            return null;
        }
    }
}
