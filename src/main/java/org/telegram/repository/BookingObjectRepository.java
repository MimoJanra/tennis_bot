package org.telegram.repository;

import org.springframework.data.repository.CrudRepository;
import org.telegram.models.BookingObject;

import java.util.List;

public interface BookingObjectRepository extends CrudRepository<BookingObject, Long> {
    List<BookingObject> findByTypeId(long id);
    List<BookingObject> findByName(String name);
}
