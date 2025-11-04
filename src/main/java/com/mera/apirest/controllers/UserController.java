package com.mera.apirest.controllers;

import com.mera.apirest.dto.user.CreateUserRequest;
import com.mera.apirest.dto.user.CreateUserResponse;
import com.mera.apirest.dto.user.UpdateUserRequest;
import com.mera.apirest.models.User;
import com.mera.apirest.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<CreateUserResponse> create(@RequestBody CreateUserRequest request) {
        CreateUserResponse user = userService.create(request);
        return ResponseEntity.ok(user);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            CreateUserResponse response = userService.findById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", e.getMessage(),
                    "statusCode", HttpStatus.NOT_FOUND.value()
            ));
        }
    }

    @PutMapping(value = "/upload/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @ModelAttribute UpdateUserRequest request
    ) {
        try {
            CreateUserResponse response = userService.updateUserWithImage(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", e.getMessage(),
                    "statusCode", HttpStatus.NOT_FOUND.value()
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", e.getMessage(),
                    "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value()
            ));
        }
    }

}
