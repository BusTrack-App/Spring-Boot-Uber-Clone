package com.mera.apirest.services;

import com.mera.apirest.dto.role.RoleDTO;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

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
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizar campos básicos
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName());
        }

        if (request.getLastname() != null && !request.getLastname().trim().isEmpty()) {
            user.setLastname(request.getLastname());
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone());
        }

        // Procesar imagen si existe
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            try {
                // Crear directorio si no existe
                String uploadDir = "uploads/users/" + user.getId();
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Obtener extensión del archivo
                String originalFilename = request.getFile().getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }

                // Generar nombre único para evitar conflictos
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
                Path filePath = uploadPath.resolve(uniqueFilename);

                // Copiar archivo
                Files.copy(request.getFile().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Guardar ruta en formato web (con barras normales)
                String webPath = "/uploads/users/" + user.getId() + "/" + uniqueFilename;
                user.setImage(webPath);

                System.out.println("Imagen guardada exitosamente en: " + filePath.toAbsolutePath());
                System.out.println("Ruta web guardada: " + webPath);

            } catch (IOException e) {
                System.err.println("Error al guardar la imagen: " + e.getMessage());
                e.printStackTrace();
                throw new IOException("Error al procesar la imagen: " + e.getMessage());
            }
        }

        // Guardar usuario con los cambios
        User savedUser = userRepository.save(user);

        // Obtener roles y devolver respuesta
        List<Role> roles = roleRepository.findAllByUserHasRoles_User_Id(savedUser.getId());

        return userMapper.toUserResponse(savedUser, roles);
    }
}