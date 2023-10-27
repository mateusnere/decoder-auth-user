package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDTO;
import com.ead.authuser.enums.UserStatus;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 36000)
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Object> registerUser(
        @RequestBody
        @Validated(UserDTO.UserView.RegistrationPost.class)
        @JsonView(UserDTO.UserView.RegistrationPost.class)
        UserDTO userDTO) {

        if(userService.existsByUsername(userDTO.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Username already taken!");
        }

        if(userService.existsByEmail(userDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Email already taken!");
        }

        var userModel = new UserModel();
        BeanUtils.copyProperties(userDTO, userModel);
        userModel.setUserStatus(UserStatus.ACTIVE);
        userModel.setUserType(UserType.STUDENT);
        userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

        userService.save(userModel);

        return ResponseEntity.status(HttpStatus.CREATED).body(userModel);
    }

    @GetMapping("/")
    public String index() {
        log.trace("TRACE"); // Quando queremos uma granularidade maior...
        log.debug("DEBUG"); // Verificar informações quando estamos desenvolvendo uma funcionalidade (não recomendado utilizar em produção)
        log.info("INFO"); // Um detalhamento um pouco menor do que o debug mas sem deixar de ter o controle das informações
        log.warn("WARN"); // Quando queremos apresentar um log de ALERTA
        log.error("ERROR"); // Quando algo dá errado no sistema. Uma boa prática é utilizar esse nível de log dentro de um catch.
        /* O nível de log padrão do springboot é INFO. Se não definirmos um outro via application.properties (ou application.yaml) ou na inicialização da execução,
            não será printado no console os logs com nível de detalhamento maior (trace e debug), apenas os de nível de detalhamento menor (warn e error, além do próprio info).
         */
        return "Logging Spring Boot...";
    }
    
}
