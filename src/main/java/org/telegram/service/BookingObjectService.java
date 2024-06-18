package org.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.models.BookingObject;
import org.telegram.repository.BookingObjectRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Service
public class BookingObjectService {

    private final BookingObjectRepository bookingObjectRepository;

    public List<BookingObject> findAll() {
        return StreamSupport
                .stream(bookingObjectRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public void save(BookingObject bookingObject) {
        bookingObjectRepository.save(bookingObject);
    }

    public void delete(BookingObject bookingObject) {
        bookingObjectRepository.delete(bookingObject);
    }

    public void deleteById(Long id) {
        bookingObjectRepository.deleteById(id);
    }

    public Optional<BookingObject> findById(Long id) {
        return bookingObjectRepository.findById(id);
    }

    public List<BookingObject> findAvailableByDate(LocalDate date) {
        return bookingObjectRepository.findByDate(date).stream()
                .filter(bo -> bo.getAvailableSlots() > 0)
                .collect(Collectors.toList());
    }
}
