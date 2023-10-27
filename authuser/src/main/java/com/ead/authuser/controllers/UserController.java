package com.ead.authuser.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.log4j.Log4j2;
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

import com.ead.authuser.dtos.UserDTO;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specifications.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 36000)
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserModel>> getAllUsers(
        SpecificationTemplate.UserSpec spec,
        @PageableDefault(page = 0, size = 10, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<UserModel> userModelPage = userService.findAll(spec, pageable);

        if(!userModelPage.isEmpty()) {
            for (UserModel userModel : userModelPage.toList()) {
                userModel.add(linkTo(methodOn(UserController.class).getOneUser(userModel.getUserId())).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(userModelPage);
    }

    @GetMapping("{userId}")
    public ResponseEntity<Object> getOneUser(@PathVariable(value = "userId") UUID userId) {
        Optional<UserModel> userModelOptional = userService.findById(userId);
        log.debug("[GET getOneUser] UserId {} received!", userId);
        return userModelOptional.isPresent() ?
                ResponseEntity.status(HttpStatus.OK).body(userModelOptional.get()) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("User doesn't exist!");
    }

    @DeleteMapping("{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable(value = "userId") UUID userId) {
        log.debug("[DELETE deleteUser] UserId {} received!", userId);
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if(userModelOptional.isEmpty()) {
            log.warn("[DELETE deleteUser] User {} doesn't exist!", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User doesn't exist!");
        }

        userService.delete(userModelOptional.get());
        log.info("[DELETE deleteUser] User deleted successfully! UserId: {}", userId);
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully!");
    }

    @PutMapping("{userId}")
    public ResponseEntity<Object> updateUser(
        @PathVariable(value = "userId") UUID userId,
        @RequestBody 
        @Validated(UserDTO.UserView.UserPut.class)
        @JsonView(UserDTO.UserView.UserPut.class) UserDTO userDTO) {

            log.debug("[PUT updateUser] UserDTO received: {}", userDTO.toString());
            Optional<UserModel> userModelOptional = userService.findById(userId);
            if(userModelOptional.isEmpty()) {
                log.warn("[PUT updateUser] UserID {} doesn't exist!", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User doesn't exist!");
            }

            var userModel = userModelOptional.get();
            userModel.setFullName(userDTO.getFullName());
            userModel.setCpf(userDTO.getCpf());
            userModel.setPhoneNumber(userDTO.getPhoneNumber());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
            userService.save(userModel);
            log.debug("[PUT updateUser] User updated {}", userModel.toString());
            log.info("[PUT updateUser] User {} updated successfully!", userModel.getUserId());

            return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }

    @PutMapping("{userId}/password")
    public ResponseEntity<Object> updatePassword(
        @PathVariable(value = "userId") UUID userId,
        @RequestBody 
        @Validated(UserDTO.UserView.PasswordPut.class)
        @JsonView(UserDTO.UserView.PasswordPut.class) UserDTO userDTO) {

            log.debug("[PUT updatePassword] UserId {} received!", userId);
            Optional<UserModel> userModelOptional = userService.findById(userId);
            if(userModelOptional.isEmpty()) {
                log.warn("[PUT updatePassword] UserID {} doesn't exist!", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User doesn't exist!");
            }

            if(!userDTO.getOldPassword().equals(userModelOptional.get().getPassword())) {
                log.warn("[PUT updatePassword] Old password doesn't match. UserID {}", userId);
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Old password doesn't match!");
            }

            var userModel = userModelOptional.get();
            userModel.setPassword(userDTO.getPassword());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
            userService.save(userModel);
            log.info("[PUT updatePassword] Password updated successfully. UserID: {}", userId);
            return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully!");
    }

    @PutMapping("{userId}/image")
    public ResponseEntity<Object> updateImage(
        @PathVariable(value = "userId") UUID userId,
        @RequestBody 
        @Validated(UserDTO.UserView.ImagePut.class)
        @JsonView(UserDTO.UserView.ImagePut.class) UserDTO userDTO) {

            log.debug("[PUT updateImage] UserID {} received!", userId);
            Optional<UserModel> userModelOptional = userService.findById(userId);

            if(userModelOptional.isEmpty()) {
                log.warn("[PUT updateImage] UserID {} doesn't exist!", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User doesn't exist!");
            }

            var userModel = userModelOptional.get();
            userModel.setImageUrl(userDTO.getImageUrl());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
            userService.save(userModel);
            log.info("[PUT updateImage] Image updated successfully. UserID: {}", userId);

            return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }

}
