package com.vocacional.prestamoinso.Controller;

import com.vocacional.prestamoinso.DTO.PrestamoDTO;
import com.vocacional.prestamoinso.DTO.ReniecResponseDTO;
import com.vocacional.prestamoinso.DTO.SunatResponseDTO;
import com.vocacional.prestamoinso.Entity.Cliente;
import com.vocacional.prestamoinso.Entity.CronogramaPagos;
import com.vocacional.prestamoinso.Entity.Prestamo;
import com.vocacional.prestamoinso.Mapper.PrestamoMapper;
import com.vocacional.prestamoinso.Repository.ClienteRepository;
import com.vocacional.prestamoinso.Repository.PrestamoRepository;
import com.vocacional.prestamoinso.Service.ClienteService;
import com.vocacional.prestamoinso.Service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PrestamoMapper prestamoMapper;

    @PostMapping("/actualizar-deudas")
    public ResponseEntity<Map<String, Object>> actualizarDeudas() {
        prestamoService.actualizarEstadoPrestamos();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Actualización de estados completada");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.OK.value());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/filtrar")
    public ResponseEntity<List<Prestamo>> filtrarPrestamosPorEstado(@RequestParam String estado) {
        List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorEstado(estado);
        return ResponseEntity.ok(prestamos);
    }

    @GetMapping("/listar")
    public List<Prestamo> obtenerPrestamosOrdenadosPorEstado() {
        return prestamoService.findAllByOrderByFechaCreacionDesc();
    }

    @PostMapping("/crear")
    public ResponseEntity<Map<String, String>> crearPrestamo(@RequestBody PrestamoDTO request) {
        // Validar el DNI del cliente con RENIEC
        if (request.getNroDocumento().length() != 11) {
            ReniecResponseDTO datosReniec = clienteService.validarDNI(request.getNroDocumento());
            if (datosReniec == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("mensaje", "DNI no encontrado en RENIEC."));
            }
        } else {
            SunatResponseDTO datosSunat = clienteService.validarRUC(request.getNroDocumento());
            if (datosSunat == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("mensaje", "RUC no encontrado en SUNAT."));
            }

            // Verificar el estado del RUC
            if ("BAJA DE OFICIO".equals(datosSunat.getEstado())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("mensaje", "El cliente está en estado de baja de oficio, no se puede crear un préstamo."));
            }
        }

        // Verificar si el cliente existe en la base de datos
        Cliente cliente = clienteRepository.findByNroDocumento(request.getNroDocumento());
        if (cliente == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("mensaje", "Cliente no encontrado."));
        }

        // Crear el préstamo
        try {
            prestamoService.crearPrestamo(
                    cliente.getNroDocumento(),
                    request.getMonto(),
                    request.getPlazo(),
                    request.getInteres()
            );

            // Retornar una respuesta JSON exitosa
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Préstamo creado exitosamente.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            // Retornar el mensaje de error como respuesta JSON
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }



    @GetMapping("/prestamoPorDni/{dni}")
    public ResponseEntity<List<PrestamoDTO>> getPrestamosporDNI(@PathVariable String dni) {
        List<Prestamo> prestamos = prestamoService.findByClienteNroDocumento(dni);

        for (Prestamo prestamo : prestamos) {
            prestamo.setCronogramaPagos(prestamoService.obtenerCronogramaConEstadosActualizados(prestamo));
        }

        List<PrestamoDTO> prestamoDTO = prestamos.stream()
                .map(prestamoMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(prestamoDTO);
    }

    @PutMapping("/marcarPagado/{id}")
    public ResponseEntity<Void> marcarComoPagado(@PathVariable Long id) {
        prestamoService.marcarComoPagado(id);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/generarPDF/{id}")
    public ResponseEntity<byte[]> generarPDF(@PathVariable Long id) {
        CronogramaPagos pago = prestamoService.obtenerPagoPorId(id);


        byte[] pdfBytes = prestamoService.generarPDF(pago);

        // Retornar el PDF como un archivo descargable
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=prestamo_pagado_" + id + ".pdf");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarPrestamo(@PathVariable Long id) {
        prestamoService.eliminarPrestamo(id);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = prestamoService.generarPdf(id); // Llamar al servicio para generar el PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "prestamo_" + id + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Manejar errores si no se puede generar el PDF
        }
    }


    @GetMapping("/prestamos-pendientes")
    public List<Prestamo> obtenerPrestamosPendientes() {
        return prestamoService.listarPrestamosPendientes();
    }
}
