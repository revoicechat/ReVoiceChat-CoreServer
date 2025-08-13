package fr.revoicechat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.revoicechat.model.UserRole;
import fr.revoicechat.model.UserRole.UserRolePK;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRolePK> {}
