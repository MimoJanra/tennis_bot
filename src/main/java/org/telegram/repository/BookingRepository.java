package org.telegram.repository;

import jakarta.annotation.Nonnull;
import org.springframework.data.repository.CrudRepository;
import org.telegram.models.Booking;
import org.telegram.models.BookingObject;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends CrudRepository<Booking, Long> {
    @Nonnull
    List<Booking> findAll();
    List<Booking> findByDateAndCourt(LocalDate date, BookingObject court);
    List<Booking> findByUserId(long userId);
}
