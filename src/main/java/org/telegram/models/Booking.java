package org.telegram.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private int trainingHour;
    private int trainingMinute;
    private int duration;
    private String location;
    private String name;
    private String description;
    private boolean recurring;
    private boolean isApproved;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "booking_object_id")
    private BookingObject court;

    public String getFullText(boolean isAdmin) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        String text = court.getName() + "\n"
                + date.format(dateFormatter) + "\n"
                + String.format("%02d:%02d", trainingHour, trainingMinute) + "\n"
                + duration + " минут\n"
                + location + "\n"
                + name + "\n"
                + description;
        if (recurring) {
            text += "\nТренировка будет повторяться раз в неделю";
        }
        if (isAdmin) {
            text += "\n@" + user.getUsername();
        }

        return text;
    }
}
