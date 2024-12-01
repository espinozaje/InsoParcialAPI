package com.vocacional.prestamoinso.Service;


import com.vocacional.prestamoinso.DTO.ClienteDTO;
import com.vocacional.prestamoinso.DTO.ReniecResponseDTO;
import com.vocacional.prestamoinso.DTO.SunatResponseDTO;
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

    private final String apiUrldni = "https://api.apis.net.pe/v2/reniec/dni?numero={nroDocumento}";
    private final String apiUrlRuc = "https://api.apis.net.pe/v2/sunat/ruc?numero={nroDocumento}";
    private final String token = "apis-token-10790.r2YzveVxIjOD5d2EFuGrst1uQFnquzDJ"; // Reemplaza con tu token
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClienteRepository clienteRepository;

    public Cliente registrarCliente(ClienteDTO registroClienteDTO) {
        String nroDocumento = registroClienteDTO.getNroDocumento();
        if (nroDocumento.length() == 11) {
            // Si es un RUC, validar con la API de SUNAT
            SunatResponseDTO datosSunat = validarRUC(registroClienteDTO.getNroDocumento());

            if (datosSunat == null) {
                throw new RuntimeException("RUC no válido o no encontrado en SUNAT");
            }

            // Crear el cliente con los datos obtenidos de la API SUNAT
            Cliente cliente = new Cliente();
            cliente.setNroDocumento(registroClienteDTO.getNroDocumento());
            cliente.setNombre(datosSunat.getRazonSocial());
            cliente.setCreateAt(LocalDateTime.now());
            cliente.setNacionalidad("PERUANO");
            cliente.setRole(ERole.USER);
            cliente.setDireccion(datosSunat.getDireccion());
            cliente.setDistrito(datosSunat.getDistrito());
            cliente.setProvincia(datosSunat.getProvincia());
            cliente.setDepartamento(datosSunat.getDepartamento());

            return clienteRepository.save(cliente);
        } else {
            // Si es un DNI, utilizar el método de RENIEC
            ReniecResponseDTO datosReniec = validarDNI(registroClienteDTO.getNroDocumento());

            if (datosReniec == null) {
                throw new RuntimeException("DNI no válido o no encontrado en RENIEC");
            }

            // Crear el cliente con los datos obtenidos de la API RENIEC
            Cliente cliente = new Cliente();
            cliente.setNroDocumento(registroClienteDTO.getNroDocumento());
            cliente.setNombre(datosReniec.getNombres());
            cliente.setApellidoPaterno(datosReniec.getApellidoPaterno());
            cliente.setApellidoMaterno(datosReniec.getApellidoMaterno());
            cliente.setCreateAt(LocalDateTime.now());
            cliente.setNacionalidad("PERUANO");
            cliente.setRole(ERole.USER);

            return clienteRepository.save(cliente);
        }
    }


    public ReniecResponseDTO validarDNI(String dni) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Realiza la llamada a la API de RENIEC
            ResponseEntity<ReniecResponseDTO> response = restTemplate.exchange(
                    apiUrldni, HttpMethod.GET, entity, ReniecResponseDTO.class, dni
            );

            return response.getBody(); // Devuelve la respuesta si es válida
        } catch (Exception e) {
            // Si hay algún error o el DNI no es válido, captura la excepción y retorna null
            return null;
        }
    }


    public SunatResponseDTO validarRUC(String ruc) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);  // Tu token de API

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Realiza la llamada a la API de SUNAT
            ResponseEntity<SunatResponseDTO> response = restTemplate.exchange(
                    apiUrlRuc, HttpMethod.GET, entity, SunatResponseDTO.class, ruc
            );

            return response.getBody();  // Devuelve la respuesta si es válida
        } catch (Exception e) {
            // Si hay algún error o el RUC no es válido, captura la excepción y retorna null
            return null;
        }
    }
}
