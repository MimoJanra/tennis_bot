package org.telegram.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "booking_objects")
public class BookingObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private String location;
    private boolean recurring;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
