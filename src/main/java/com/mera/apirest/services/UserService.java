package com.mera.apirest.services;

import com.mera.apirest.dto.user.CreateUserRequest;
import com.mera.apirest.models.Role;
import com.mera.apirest.models.User;
import com.mera.apirest.models.UserHasRoles;
import com.mera.apirest.repositories.RoleRepository;
import com.mera.apirest.repositories.UserHasRolesRepository;
import com.mera.apirest.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserHasRolesRepository userHasRolesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email)) {
            throw new RuntimeException("El correo ya está registrado");
        }

        User user = new User();
        user.setName(request.name);
        user.setLastname(request.lastname);
        user.setPhone(request.phone);
        user.setEmail(request.email);

        String encryptedPassword = passwordEncoder.encode(request.password);
        user.setPassword(encryptedPassword);

        User savedUser = userRepository.save(user);

        Role clientRole = roleRepository.findById("CLIENT")
                .orElseThrow(() -> new RuntimeException("El rol CLIENT no existe"));

        UserHasRoles userHasRoles = new UserHasRoles(savedUser, clientRole);
        userHasRolesRepository.save(userHasRoles);

        return savedUser;
    }
}