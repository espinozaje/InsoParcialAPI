package com.vocacional.prestamoinso.Controller;


import com.vocacional.prestamoinso.DTO.TrabajadorDTO;
import com.vocacional.prestamoinso.Entity.Trabajador;
import com.vocacional.prestamoinso.Service.JwtUtilService;
import com.vocacional.prestamoinso.Service.TrabajadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/trabajador")
public class TrabajadorController {

    @Autowired
    private TrabajadorService trabajadorService;

    @Autowired
    private JwtUtilService jwtUtilService;



    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String username, @RequestParam String password) {
        Map<String, String> response = new HashMap<>();
        Trabajador trabajador = trabajadorService.login(username, password);


        if (trabajador != null) {

            String token = jwtUtilService.generateToken(trabajador);

            response.put("message", "Login exitoso");
            response.put("token", token);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Credenciales incorrectas");
            return ResponseEntity.status(401).body(response);
        }
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            trabajadorService.deleteUser(id);
            return ResponseEntity.ok("Usuario eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar el usuario: " + e.getMessage());
        }
    }


    @GetMapping("/me")
    public ResponseEntity<Trabajador> getUserInfo(@RequestHeader("Authorization") String token) {

        String jwt = token.substring(7);


        String username = jwtUtilService.extractUsername(jwt);


        Trabajador usuario = trabajadorService.findByUsername(username);

        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
