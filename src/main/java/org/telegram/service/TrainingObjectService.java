package org.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.models.TrainingObject;
import org.telegram.repository.TrainingObjectRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Service
public class TrainingObjectService {

    private final TrainingObjectRepository trainingObjectRepository;

    public List<TrainingObject> findAll() {
        return StreamSupport
                .stream(trainingObjectRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public void save(TrainingObject trainingObject) {
        if (trainingObject.getAvailableSlots() == 0) {
            trainingObject.setAvailableSlots(trainingObject.getParticipants());
        }
        trainingObjectRepository.save(trainingObject);
    }

    public void delete(TrainingObject trainingObject) {
        trainingObjectRepository.delete(trainingObject);
    }

    public void deleteById(Long id) {
        trainingObjectRepository.deleteById(id);
    }

    public Optional<TrainingObject> findById(Long id) {
        return trainingObjectRepository.findById(id);
    }

    public List<TrainingObject> findAvailableByDate(LocalDate date) {
        List<TrainingObject> trainingObjects = trainingObjectRepository.findByDate(date);
        System.out.println("Training objects on date " + date + ": " + trainingObjects);
        return trainingObjects.stream()
                .filter(bo -> bo.getAvailableSlots() > 0)
                .collect(Collectors.toList());
    }
}
