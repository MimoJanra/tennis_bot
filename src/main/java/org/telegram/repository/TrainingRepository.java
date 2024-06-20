package org.telegram.repository;

import jakarta.annotation.Nonnull;
import org.springframework.data.repository.CrudRepository;
import org.telegram.models.Training;
import org.telegram.models.TrainingObject;

import java.time.LocalDate;
import java.util.List;

public interface TrainingRepository extends CrudRepository<Training, Long> {
    @Nonnull
    List<Training> findAll();

    List<Training> findByDateAndTrainingObject(LocalDate date, TrainingObject trainingObject);

    List<Training> findByUserId(long userId);
}
