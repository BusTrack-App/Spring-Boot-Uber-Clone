package com.mera.apirest.controllers;

import com.mera.apirest.dto.user.CreateUserRequest;
import com.mera.apirest.dto.user.CreateUserResponse;
import com.mera.apirest.dto.user.LoginRequest;
import com.mera.apirest.dto.user.LoginResponse;
import com.mera.apirest.models.User;
import com.mera.apirest.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping(value = "/register")
    public ResponseEntity<?> create(@RequestBody CreateUserRequest request) {
        try {
            CreateUserResponse user = userService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage(),
                    "statusCode", HttpStatus.BAD_REQUEST.value()
            ));
        }
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", e.getMessage(),
                    "statusCode", HttpStatus.UNAUTHORIZED.value()
            ));
        }
    }

}
