package org.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.repository.TypeRepository;
import org.telegram.models.Type;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TypeService {

    private final TypeRepository typeRepository;

    public List<Type> findAll() {
        return typeRepository.findAll();
    }

    public Optional<Type> findById(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }

        return typeRepository.findById(Long.parseLong(id));
    }

    public void save(Type type) {
        typeRepository.save(type);
    }

}
