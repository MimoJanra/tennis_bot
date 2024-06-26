package org.telegram.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;
    private boolean isApproved;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "booking_object_id")
    private BookingObject bookingObject;

    public String getFullText(boolean isAdmin) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        String text = bookingObject.getName() + "\n"
                + timeStart.toLocalDate().format(dateFormatter) + "\n"
                + timeStart.toLocalTime() + " - " + timeEnd.toLocalTime();
        if (isAdmin) {
            text += "\n@" + user.getUsername();
        }

        return text;
    }
}