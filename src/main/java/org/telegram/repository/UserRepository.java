package org.telegram.repository;

import org.springframework.data.repository.CrudRepository;
import org.telegram.models.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
