package org.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.models.BookingObject;
import org.telegram.repository.BookingObjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BookingObjectService {

    private final BookingObjectRepository bookingObjectRepository;

    public Optional<BookingObject> findById(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }

        return bookingObjectRepository.findById(Long.parseLong(id));
    }

    public List<BookingObject> findByTypeId(String id) {
        if (id == null || id.isEmpty()) {
            return new ArrayList<>();
        }

        return bookingObjectRepository.findByTypeId(Long.parseLong(id));
    }

    public List<BookingObject> findByName(String name) {
        return bookingObjectRepository.findByName(name);
    }

    public void save(BookingObject bookingObject) {
        bookingObjectRepository.save(bookingObject);
    }

    public void delete(BookingObject object) {
        bookingObjectRepository.delete(object);
    }

}
