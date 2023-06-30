package com.ead.authuser.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ead.authuser.dtos.UserDTO;
import com.ead.authuser.enums.UserStatus;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;

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
    
}
