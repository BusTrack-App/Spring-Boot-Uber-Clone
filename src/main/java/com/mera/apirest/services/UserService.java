package com.mera.apirest.services;

import com.mera.apirest.dto.role.RoleDTO;
import com.mera.apirest.dto.user.CreateUserRequest;
import com.mera.apirest.dto.user.CreateUserResponse;
import com.mera.apirest.dto.user.LoginRequest;
import com.mera.apirest.dto.user.LoginResponse;
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
    public CreateUserResponse create(CreateUserRequest request) {
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

        CreateUserResponse createUserResponse = new CreateUserResponse();
        createUserResponse.setId(savedUser.getId());
        createUserResponse.setName(savedUser.getName());
        createUserResponse.setLastname(savedUser.getLastname());
        createUserResponse.setImage(savedUser.getImage());
        createUserResponse.setPhone(savedUser.getPhone());
        createUserResponse.setEmail(savedUser.getEmail());

        List<Role> roles = roleRepository.findAllByUserHasRoles_User_Id(savedUser.getId());
        List<RoleDTO> roleDTOS = roles.stream()
                .map(role -> new RoleDTO(role.getId(), role.getName(), role.getImage(), role.getRoute()))
                .toList();
        createUserResponse.setRoles(roleDTOS);

        return createUserResponse;
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
}