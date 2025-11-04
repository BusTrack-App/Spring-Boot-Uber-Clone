package com.mera.apirest.repositories;

import com.mera.apirest.models.UserHasRoles;
import com.mera.apirest.models.id.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHasRolesRepository extends JpaRepository<UserHasRoles, UserRoleId> {
}
