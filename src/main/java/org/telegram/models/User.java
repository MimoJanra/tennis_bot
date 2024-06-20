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
    private Long chatId;

    @OneToMany(mappedBy = "user")
    private List<Training> trainings;

    public String getName() {
        return username;
    }
}
