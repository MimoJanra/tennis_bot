package org.telegram.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "booking_objects")
public class BookingObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
