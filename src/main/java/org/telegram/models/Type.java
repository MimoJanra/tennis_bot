package org.telegram.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "types")
public class Type {

    @Id
    private String id;
    private String name;
}