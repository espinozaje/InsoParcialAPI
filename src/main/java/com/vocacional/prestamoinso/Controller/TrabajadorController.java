package com.vocacional.prestamoinso.Controller;


import com.vocacional.prestamoinso.DTO.TrabajadorDTO;
import com.vocacional.prestamoinso.Entity.Trabajador;
import com.vocacional.prestamoinso.Entity.User;
import com.vocacional.prestamoinso.Repository.TrabajadorRepository;
import com.vocacional.prestamoinso.Repository.UserRepository;
import com.vocacional.prestamoinso.Service.JwtUtilService;
import com.vocacional.prestamoinso.Service.TrabajadorService;
import com.vocacional.prestamoinso.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/trabajador")
public class TrabajadorController {

    @Autowired
    private TrabajadorService trabajadorService;

    @Autowired
    private JwtUtilService jwtUtilService;
    @Autowired
    private TrabajadorRepository trabajadorRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;


    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String username, @RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        Optional<Trabajador> optionalTrabajador = trabajadorRepository.findByUsername(username);
        if (optionalTrabajador.isPresent()) {
            Trabajador trabajador = optionalTrabajador.get();

            if (!passwordEncoder.matches(password, trabajador.getPassword())) {
                response.put("message", "Contraseña incorrecta");
                return ResponseEntity.badRequest().body(response);
            }


            String token = jwtUtilService.generateToken(trabajador);

            response.put("message", "Inicio de sesión exitoso");
            response.put("token", token);


            response.put("requiresPasswordChange", trabajador.isNeedsPasswordChange());

            return ResponseEntity.ok(response);
        }
        response.put("message", "Trabajador no registrado");
        return ResponseEntity.badRequest().body(response);
    }


    @PostMapping("/register")
    public void registerWorker(@Validated @RequestBody TrabajadorDTO trabajadorDTO) {
        userService.registerTrabajador(trabajadorDTO);
        ResponseEntity.ok().body("{\"message\": \"Usuario registrado con éxito.\"}");
    }


    @PostMapping("/register-admin")
    public void registerAdmin(@Validated @RequestBody TrabajadorDTO trabajadorDTO) {
        userService.registerAdmin(trabajadorDTO);
        ResponseEntity.ok().body("{\"message\": \"Usuario registrado con éxito.\"}");
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
    public ResponseEntity<User> getUserInfo(@RequestHeader("Authorization") String token) {

        String jwt = token.substring(7);


        String username = jwtUtilService.extractUsername(jwt);


        User usuario = userRepository.findByUsername(username);

        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestParam String username, @RequestParam String newPassword) {
        Map<String, Object> response = new HashMap<>();

        Optional<Trabajador> optionalTrabajador = trabajadorRepository.findByUsername(username);
        if (optionalTrabajador.isPresent()) {
            Trabajador trabajador = optionalTrabajador.get();


            trabajador.setPassword(passwordEncoder.encode(newPassword));
            trabajador.setNeedsPasswordChange(false);
            trabajadorRepository.save(trabajador);

            response.put("message", "Contraseña cambiada exitosamente");
            return ResponseEntity.ok(response);
        }

        response.put("message", "Trabajador no encontrado");
        return ResponseEntity.badRequest().body(response);
    }



    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            trabajadorService.generateResetPasswordToken(email);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(Map.of("message", "Se ha enviado un correo con instrucciones para restablecer su contraseña."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(Map.of("error", "Error al generar el token de recuperación: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        try {
            trabajadorService.resetPassword(token, newPassword);

            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Error al restablecer la contraseña: " + e.getMessage()));
        }
    }

}
