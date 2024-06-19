package org.telegram.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "training_objects")
public class TrainingObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDate date;
    private String trainingHour;
    private String trainingMinute;
    private int duration;
    private String location;
    private boolean recurring;
    private int participants;
    private double cost;
    private int availableSlots;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public void bookSlot() {
        if (availableSlots > 0) {
            availableSlots--;
        }
    }
}
