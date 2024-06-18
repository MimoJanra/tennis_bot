package org.telegram.repository;

import org.springframework.data.repository.CrudRepository;
import org.telegram.models.BookingObject;

import java.time.LocalDate;
import java.util.List;

public interface BookingObjectRepository extends CrudRepository<BookingObject, Long> {
    List<BookingObject> findByDate(LocalDate date);
}
