package org.telegram.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.telegram.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByChatId(Long chatId);
}
