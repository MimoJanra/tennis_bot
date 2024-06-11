package org.telegram.repository;

import jakarta.annotation.Nonnull;
import org.springframework.data.repository.CrudRepository;
import org.telegram.models.Type;

import java.util.List;

public interface TypeRepository extends CrudRepository<Type, Long> {

    @Nonnull
    List<Type> findAll();
}
