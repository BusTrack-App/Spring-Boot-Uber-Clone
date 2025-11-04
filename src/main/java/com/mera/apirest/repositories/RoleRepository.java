package com.mera.apirest.repositories;

import com.mera.apirest.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
    boolean existsByName(String name);
}
