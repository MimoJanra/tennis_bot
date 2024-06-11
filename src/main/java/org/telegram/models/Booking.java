package org.telegram.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;

    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;
    private boolean isApproved;

    @DBRef
    private User user;

    @DBRef
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