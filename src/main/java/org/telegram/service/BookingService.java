package org.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.models.Booking;
import org.telegram.models.BookingObject;
import org.telegram.repository.BookingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> findById(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }

        return bookingRepository.findById(Long.parseLong(id));
    }

    public List<Booking> findByDateAndCourt(LocalDate date, BookingObject bookingObject) {
        return bookingRepository.findByDateAndBookingObject(date, bookingObject);
    }

    public List<Booking> findByUserId(long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public void save(Booking booking) {
        bookingRepository.save(booking);
    }

    public void delete(Booking booking) {
        bookingRepository.delete(booking);
    }
}
