package com.examly.springapp.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.examly.springapp.entity.Booking;
import com.examly.springapp.exception.BookingNotFoundException;
import com.examly.springapp.service.BookingService;

@RestController
public class BookingController {
    @Autowired private com.examly.springapp.repository.UserRepo users;
    private void requireUser(int userId, org.springframework.security.core.Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
        var user = users.findByEmail(auth.getName());
        if (!admin && (user == null || user.getUserId() != userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Not your booking");
        }
    }
    @Autowired
    private BookingService bookingService;

    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/api/booking")
    public ResponseEntity<List<Booking>> getAll(){
        List<Booking> all=bookingService.getAllBookings();
        return ResponseEntity.ok(all);
    }

    @PostMapping("/api/booking/{userId}/{movieId}")
    public ResponseEntity<Booking> add(@PathVariable int userId,@PathVariable Long movieId,@RequestBody Booking book, org.springframework.security.core.Authentication auth){
        requireUser(userId, auth);
        Booking book1=bookingService.createBooking(userId,movieId,book);
        return ResponseEntity.status(201).body(book1);

    }
    @GetMapping("/api/booking/{bookingId}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long bookingId, org.springframework.security.core.Authentication auth){
        Booking b=bookingService.getBookingById(bookingId);
        if(b==null){
            throw new BookingNotFoundException("Booking not found");

        }
        requireUser(b.getUser().getUserId(), auth);
        return ResponseEntity.status(200).body(b);
    }
    @DeleteMapping("/api/booking/{bookingId}")
    public ResponseEntity<Boolean> del(@PathVariable Long bookingId, org.springframework.security.core.Authentication auth){
        Booking existing=bookingService.getBookingById(bookingId);
        if(existing==null) throw new BookingNotFoundException("Booking not found");
        requireUser(existing.getUser().getUserId(), auth);
        boolean check=bookingService.deleteBooking(bookingId);
        if(!check){
            throw new BookingNotFoundException("Booking not found");
        }
        return ResponseEntity.status(200).body(check);
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("api/booking/movie/{movieId}")
    public ResponseEntity<List<Booking>> getBookingBymovieId(@PathVariable Long movieId){
        List<Booking> b=bookingService.getBookingbyMovie(movieId);

        return ResponseEntity.status(200).body(b);
    }
    @GetMapping("api/booking/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingbyUserId(@PathVariable int userId, org.springframework.security.core.Authentication auth){
        requireUser(userId, auth);
        List<Booking> b=bookingService.getBookingByUserId(userId);

        return ResponseEntity.status(200).body(b);
    }


}
