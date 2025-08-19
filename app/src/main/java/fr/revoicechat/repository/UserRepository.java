package fr.revoicechat.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.revoicechat.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
  User findByLogin(String login);
}
