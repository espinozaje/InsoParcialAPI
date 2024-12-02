package com.vocacional.prestamoinso.Controller;

import com.vocacional.prestamoinso.DTO.ClienteDTO;
import com.vocacional.prestamoinso.DTO.ReniecResponseDTO;
import com.vocacional.prestamoinso.DTO.SunatResponseDTO;
import com.vocacional.prestamoinso.Entity.Cliente;
import com.vocacional.prestamoinso.Exception.ConflictException;
import com.vocacional.prestamoinso.Repository.ClienteRepository;
import com.vocacional.prestamoinso.Service.ClienteService;
import com.vocacional.prestamoinso.Service.JwtUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private JwtUtilService jwtUtilService;

    @Autowired
    private ClienteRepository clienteRepository;

    @PostMapping("/registrar")
    public ResponseEntity<Cliente> registrarCliente(@RequestBody ClienteDTO registroClienteDTO) {
        Cliente cliente;
        // Si el cliente ya existe, no lanzamos la excepción Conflict
        if (clienteRepository.existsByNroDocumento(registroClienteDTO.getNroDocumento())) {
            cliente = clienteRepository.findByNroDocumento(registroClienteDTO.getNroDocumento());
        } else {
            cliente = clienteService.registrarCliente(registroClienteDTO);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }





    @GetMapping("/{dni}")
    public ResponseEntity<?> obtenerDatosCliente(@PathVariable String dni) {


        if (dni.length() == 11) {
            SunatResponseDTO datosCliente = clienteService.validarRUC(dni);
            if (datosCliente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // No se encontró el cliente en RENIEC
            }
            return ResponseEntity.ok(datosCliente);
        } else {
            ReniecResponseDTO datosCliente = clienteService.validarDNI(dni);
            if (datosCliente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // No se encontró el cliente en RENIEC
            }
            return ResponseEntity.ok(datosCliente);
        }
    }
}