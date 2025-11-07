package com.mera.apirest.services;

import com.mera.apirest.dto.user.*;
import com.mera.apirest.dto.user.mapper.UserMapper;
import com.mera.apirest.models.Role;
import com.mera.apirest.models.User;
import com.mera.apirest.models.UserHasRoles;
import com.mera.apirest.repositories.RoleRepository;
import com.mera.apirest.repositories.UserHasRolesRepository;
import com.mera.apirest.repositories.UserRepository;
import com.mera.apirest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

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

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public LoginResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email)) {
            throw new RuntimeException("El correo ya esta registrado");
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
                .orElseThrow(() -> new RuntimeException("El rol de cliente no existe"));

        UserHasRoles userHasRoles = new UserHasRoles(savedUser, clientRole);
        userHasRolesRepository.save(userHasRoles);

        // Obtener los roles del usuario (en este caso solo CLIENT)
        List<Role> roles = roleRepository.findAllByUserHasRoles_User_Id(savedUser.getId());

        // Generar token
        String token = jwtUtil.generateToken(savedUser);

        // Construir respuesta
        LoginResponse response = new LoginResponse();
        response.setToken("Bearer " + token);
        response.setUser(userMapper.toUserResponse(savedUser, roles));

        return response;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("El Email o Password no son validos"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("El Email o Password no son validos");
        }

        String token = jwtUtil.generateToken(user);
        List<Role> roles = roleRepository.findAllByUserHasRoles_User_Id(user.getId());

        LoginResponse response = new LoginResponse();
        response.setToken("Bearer " + token);
        response.setUser(userMapper.toUserResponse(user, roles));

        return response;
    }

    @Transactional
    public CreateUserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El Email o Password no son validos"));

        List<Role> roles = roleRepository.findAllByUserHasRoles_User_Id(user.getId());

        return userMapper.toUserResponse(user, roles);
    }


    @Transactional
    public CreateUserResponse updateUserWithImage(Long id, UpdateUserRequest request) throws IOException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El Email o Password no son validos"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getLastname() != null) {
            user.setLastname(request.getLastname());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getFile() != null && !request.getFile().isEmpty()) {
            String uploadDir = "uploads/users/" + user.getId();
            String filename = request.getFile().getOriginalFilename();
            String filePath = Paths.get(uploadDir, filename).toString();

            Files.createDirectories(Paths.get(uploadDir));
            Files.copy(request.getFile().getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
            user.setImage("/" + filePath.replace("\\", "/"));
        }

        userRepository.save(user);

        List<Role> roles = roleRepository.findAllByUserHasRoles_User_Id(user.getId());

        return userMapper.toUserResponse(user, roles);
    }

}
