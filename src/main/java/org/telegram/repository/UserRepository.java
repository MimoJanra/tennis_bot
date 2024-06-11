package org.telegram.repository;

import org.springframework.data.repository.CrudRepository;
import org.telegram.models.User;

public interface UserRepository extends CrudRepository<User, Long> {
}
