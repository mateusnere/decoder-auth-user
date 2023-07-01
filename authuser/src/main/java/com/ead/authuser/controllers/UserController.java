package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDTO;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 36000)
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserModel>> getAllUsers(
        @PageableDefault(page = 0, size = 10, sort = "userId", direction = Sort.Direction.ASC) 
        Pageable pageable) {

        Page<UserModel> userModelPage = userService.findAll(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(userModelPage);
    }

    @GetMapping("{userId}")
    public ResponseEntity<Object> getOneUser(@PathVariable(value = "userId") UUID userId) {
        Optional<UserModel> userModelOptional = userService.findById(userId);
        return userModelOptional.isPresent() ?
                ResponseEntity.status(HttpStatus.OK).body(userModelOptional.get()) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("User doesn't exist!");
    }

    @DeleteMapping("{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable(value = "userId") UUID userId) {
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if(userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User doesn't exist!");
        }

        userService.delete(userModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully!");
    }

    @PutMapping("{userId}")
    public ResponseEntity<Object> updateUser(
        @PathVariable(value = "userId") UUID userId,
        @RequestBody 
        @Validated(UserDTO.UserView.UserPut.class)
        @JsonView(UserDTO.UserView.UserPut.class) UserDTO userDTO) {
            Optional<UserModel> userModelOptional = userService.findById(userId);
            if(userModelOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User doesn't exist!");
            }

            var userModel = userModelOptional.get();
            userModel.setFullName(userDTO.getFullName());
            userModel.setCpf(userDTO.getCpf());
            userModel.setPhoneNumber(userDTO.getPhoneNumber());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

            userService.save(userModel);

            return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }

    @PutMapping("{userId}/password")
    public ResponseEntity<Object> updatePassword(
        @PathVariable(value = "userId") UUID userId,
        @RequestBody 
        @Validated(UserDTO.UserView.PasswordPut.class)
        @JsonView(UserDTO.UserView.PasswordPut.class) UserDTO userDTO) {
            
            Optional<UserModel> userModelOptional = userService.findById(userId);
            if(userModelOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User doesn't exist!");
            }

            if(!userDTO.getOldPassword().equals(userModelOptional.get().getPassword())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Old password doesn't match!");
            }

            var userModel = userModelOptional.get();
            userModel.setPassword(userDTO.getPassword());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

            userService.save(userModel);
            return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully!");
    }

    @PutMapping("{userId}/image")
    public ResponseEntity<Object> updateImage(
        @PathVariable(value = "userId") UUID userId,
        @RequestBody 
        @Validated(UserDTO.UserView.ImagePut.class)
        @JsonView(UserDTO.UserView.ImagePut.class) UserDTO userDTO) {

            Optional<UserModel> userModelOptional = userService.findById(userId);

            if(userModelOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User doesn't exist!");
            }

            var userModel = userModelOptional.get();
            userModel.setImageUrl(userDTO.getImageUrl());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

            userService.save(userModel);

            return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }

}
