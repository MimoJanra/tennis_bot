package org.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.models.Training;
import org.telegram.models.TrainingObject;
import org.telegram.repository.TrainingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;

    public List<Training> findAll() {
        return trainingRepository.findAll();
    }

    public Optional<Training> findById(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }

        return trainingRepository.findById(Long.parseLong(id));
    }

    public List<Training> findByDateAndTrainingObject(LocalDate date, TrainingObject trainingObject) {
        return trainingRepository.findByDateAndTrainingObject(date, trainingObject);
    }

    public List<Training> findByUserId(long userId) {
        return trainingRepository.findByUserId(userId);
    }

    public void save(Training training) {
        trainingRepository.save(training);
    }

    public void delete(Training training) {
        trainingRepository.delete(training);
    }
}
