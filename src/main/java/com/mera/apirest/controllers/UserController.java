package com.mera.apirest.controllers;

import com.mera.apirest.dto.user.CreateUserRequest;
import com.mera.apirest.dto.user.CreateUserResponse;
import com.mera.apirest.models.User;
import com.mera.apirest.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
