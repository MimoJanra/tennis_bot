package org.telegram.repository;

import org.springframework.data.repository.CrudRepository;
import org.telegram.models.BookingObject;

public interface BookingObjectRepository extends CrudRepository<BookingObject, Long> {
}
