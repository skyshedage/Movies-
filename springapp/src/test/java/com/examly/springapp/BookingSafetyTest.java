package com.examly.springapp;
import com.examly.springapp.service.serviceImpl.BookingServiceImpl;
import com.examly.springapp.repository.*;
import com.examly.springapp.entity.*;
import com.examly.springapp.exception.BookingNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class BookingSafetyTest {
 @Mock BookingRepo bookings;
 @Mock MovieRepo movies;
 @Mock UserRepo users;
 @InjectMocks BookingServiceImpl service;
 @Test void rejectsNonPositiveSeatCountsBeforeRepositoryAccess() {
   for(int n: new int[]{0,-1,Integer.MIN_VALUE}) {
     Booking b=new Booking();b.setSeatCount(n);
     assertThrows(IllegalArgumentException.class,()->service.createBooking(1,2L,b));
   }
   verifyNoInteractions(bookings,movies,users);
 }
 @Test void rejectsOverCapacity() {
   Movie m=new Movie();m.setTotalSeats(10);
   when(movies.findForBooking(2L)).thenReturn(Optional.of(m));
   when(bookings.countBookedSeatsByMovie(2L)).thenReturn(9);
   Booking b=new Booking();b.setSeatCount(2);
   assertThrows(BookingNotFoundException.class,()->service.createBooking(1,2L,b));
   verify(bookings,never()).save(any());
 }
 @Test void rejectsOverflowSeatRequest() {
   Movie m=new Movie();m.setTotalSeats(10);
   when(movies.findForBooking(2L)).thenReturn(Optional.of(m));
   when(bookings.countBookedSeatsByMovie(2L)).thenReturn(9);
   Booking b=new Booking();b.setSeatCount(Integer.MAX_VALUE);
   assertThrows(BookingNotFoundException.class,()->service.createBooking(1,2L,b));
   verify(bookings,never()).save(any());
 }
 @Test void calculatesTrustedPriceAndCreatesNewBooking() {
   Movie m=new Movie();m.setTotalSeats(10);m.setPrice(15);
   User u=new User();
   when(movies.findForBooking(2L)).thenReturn(Optional.of(m));
   when(users.findById(1)).thenReturn(Optional.of(u));
   when(bookings.save(any())).thenAnswer(a->a.getArgument(0));
   Booking b=new Booking();b.setSeatCount(2);b.setId(123L);b.setTotalCost(-10);
   Booking result=service.createBooking(1,2L,b);
   assertNull(result.getId());assertEquals(30.0,result.getTotalCost());
   assertSame(m,result.getMovie());assertSame(u,result.getUser());
 }
}
