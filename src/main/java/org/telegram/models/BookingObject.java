package org.telegram.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalTime;

@Data
@Document(collection = "booking_objects")
public class BookingObject {

    @Id
    private String id;
    private String name;
    private String description;
    private String image;
    private LocalTime availableFrom;
    private LocalTime availableTo;

    @DBRef
    private Type type;

    public String getFullText() {
        return name + (description == null ? "" : ("\n" + description));
    }
}