package org.telegram.repository;

import org.springframework.data.repository.CrudRepository;
import org.telegram.models.TrainingObject;

import java.time.LocalDate;
import java.util.List;

public interface TrainingObjectRepository extends CrudRepository<TrainingObject, Long> {
    List<TrainingObject> findByDate(LocalDate date);
}
