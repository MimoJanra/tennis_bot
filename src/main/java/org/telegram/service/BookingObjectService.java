package org.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.models.BookingObject;
import org.telegram.repository.BookingObjectRepository;

import java.util.List;
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

    public BookingObject findById(String id) {
        return bookingObjectRepository.findById(Long.parseLong(id)).orElse(null);
    }

    public List<BookingObject> findByTypeId(String typeId) {
        return List.of();
    }
}
