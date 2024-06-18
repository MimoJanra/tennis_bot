package org.telegram.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private boolean isAdmin;

    @OneToMany(mappedBy = "user")
    private List<Booking> bookings;

    public String getName() {
        return username;
    }
}
