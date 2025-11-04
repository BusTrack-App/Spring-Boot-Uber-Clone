package com.mera.apirest.dto.user.mapper;

import com.mera.apirest.config.APIConfig;
import com.mera.apirest.dto.role.RoleDTO;
import com.mera.apirest.dto.user.CreateUserResponse;
import com.mera.apirest.models.Role;
import com.mera.apirest.models.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Component
public class UserMapper {

    public CreateUserResponse toUserResponse(User user, List<Role> roles) {
        List<RoleDTO> roleDTOS = roles.stream()
                .map(role -> new RoleDTO(role.getId(), role.getName(), role.getImage(), role.getRoute()))
                .toList();
        CreateUserResponse response = new CreateUserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setLastname(user.getLastname());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setRoles(roleDTOS);

        if (user.getImage() != null) {
            String imageUrl = APIConfig.BASE_URL + user.getImage();
            response.setImage(imageUrl);
        }
        return response;
    }

}