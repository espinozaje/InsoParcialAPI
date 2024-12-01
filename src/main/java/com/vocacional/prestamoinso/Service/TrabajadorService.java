package com.vocacional.prestamoinso.Service;


import com.vocacional.prestamoinso.Entity.Trabajador;
import com.vocacional.prestamoinso.Entity.User;
import com.vocacional.prestamoinso.Repository.TrabajadorRepository;
import com.vocacional.prestamoinso.Repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Service
public class TrabajadorService {


    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TrabajadorRepository trabajadorRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JavaMailSenderImpl mailSender;


    public void deleteUser(Long id) throws Exception {
        Trabajador trabajador = trabajadorRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con id: " + id));

        trabajadorRepository.delete(trabajador);
    }


    public void generateResetPasswordToken(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("No se encontró un usuario con ese correo.");
        }

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        userRepository.save(user);


        sendResetPasswordEmail(user.getEmail(), token);
    }

    public void resetPassword(String token, String newPassword) throws Exception {
        User user = userRepository.findByResetPasswordToken(token);
        if (user == null) {
            throw new Exception("Token inválido.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        userRepository.save(user);
    }

    private void sendResetPasswordEmail(String email, String token) throws Exception {
        String resetLink = "https://insofinal-frontend.vercel.app/reset-password?token=" + token;
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setTo(email);
        helper.setSubject("Restablecer tu contraseña en PrestamosAPP");
        helper.setText("<p>Hola,</p>" +
                "<p>Esperamos que estés teniendo un buen día. Hemos recibido una solicitud para restablecer la contraseña de tu cuenta en PrestamosAPP.</p>" +
                "<p>Si deseas restablecer tu contraseña, haz clic en el enlace de abajo:</p>" +
                "<a href=\"" + resetLink + "\">Restablecer contraseña</a>" +
                "<p>Si no solicitaste este cambio, puedes ignorar este correo.</p>" +
                "<p>Atentamente,<br>El equipo de desarrollo de PrestamosAPP</p>"+
                "<p>Tener en cuenta que el link caduce dentro de 2 horas</p>", true);
        mailSender.send(message);
    }
}
