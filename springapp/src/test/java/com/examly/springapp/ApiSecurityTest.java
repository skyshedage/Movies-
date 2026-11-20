package com.examly.springapp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.examly.springapp.repository.*;
import com.examly.springapp.entity.*;
@SpringBootTest(properties={"spring.flyway.enabled=false",
 "spring.datasource.url=jdbc:h2:mem:movies;MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
 "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop",
 "MOVIES_JWT_SECRET=test-only-placeholder-with-at-least-32-bytes"
})
@AutoConfigureMockMvc
class ApiSecurityTest {
 @Autowired MockMvc mvc;
 @Autowired UserRepo users;
 @Autowired MovieRepo movies;
 @Autowired BookingRepo bookings;
 @Autowired com.examly.springapp.configuration.JwtUtils jwt;
 @Autowired com.examly.springapp.service.BookingService bookingService;
 @Test void anonymousCannotReadUsers() throws Exception { mvc.perform(get("/api/user")).andExpect(status().isForbidden()); }
 @Test void anonymousCannotReadBookings() throws Exception { mvc.perform(get("/api/booking")).andExpect(status().isForbidden()); }
 @WithMockUser(authorities="USER") @Test void normalUserCannotCreateMovie() throws Exception {
   mvc.perform(post("/api/movie").contentType("application/json").content("{}")).andExpect(status().isForbidden());
 }
 @WithMockUser(username="viewer@example.invalid",authorities="USER") @Test void usersCannotReadSomeoneElsesBookings() throws Exception {
   mvc.perform(get("/api/booking/user/99")).andExpect(status().isForbidden());
 }
 @Test void malformedTokenReturnsUnauthorized() throws Exception {
   mvc.perform(get("/api/booking").header("Authorization","Bearer nonsense")).andExpect(status().isUnauthorized());
 }
 @Test void registrationCannotGrantAdminOrExposePassword() throws Exception {
   mvc.perform(post("/api/user/register").contentType("application/json")
    .content("{\"email\":\"registration@example.invalid\",\"password\":\"test-password\",\"username\":\"Test\",\"userRole\":\"ADMIN\"}"))
    .andExpect(status().isCreated()).andExpect(jsonPath("$.password").doesNotExist()).andExpect(jsonPath("$.userRole").value("USER"));
 }
 @Test void realBearerTokenCanBookOwnSeatsButCannotReadAnotherUsersBooking() throws Exception {
   User a=new User();a.setEmail("booking-owner@example.invalid");a.setUserRole("USER");a.setPassword("unused-test-hash");a=users.save(a);
   User b=new User();b.setEmail("booking-other@example.invalid");b.setUserRole("USER");b.setPassword("unused-test-hash");b=users.save(b);
   Movie m=new Movie();m.setTitle("Local test movie");m.setTotalSeats(3);m.setPrice(12);m=movies.save(m);
   String token=jwt.generateToken(a.getEmail());
   mvc.perform(post("/api/booking/"+a.getUserId()+"/"+m.getId()).header("Authorization","Bearer "+token)
     .contentType("application/json").content("{\"seatCount\":2,\"totalCost\":1}"))
     .andExpect(status().isCreated()).andExpect(jsonPath("$.totalCost").value(24)).andExpect(jsonPath("$.user.password").doesNotExist());
   Booking saved=bookings.findByUser(a.getUserId()).get(0);
   mvc.perform(get("/api/booking/"+saved.getId()).header("Authorization","Bearer "+jwt.generateToken(b.getEmail())))
     .andExpect(status().isForbidden());
 }
 @Test void concurrentSeatRequestsCannotOversell() throws Exception {
   User u=new User();u.setEmail("concurrent@example.invalid");u.setUserRole("USER");u=users.save(u);
   Movie m=new Movie();m.setTitle("Lock test");m.setTotalSeats(1);m.setPrice(10);m=movies.save(m);
   final int uid=u.getUserId();final long mid=m.getId();
   java.util.concurrent.ExecutorService pool=java.util.concurrent.Executors.newFixedThreadPool(2);
   java.util.concurrent.CountDownLatch start=new java.util.concurrent.CountDownLatch(1);
   java.util.concurrent.Callable<Boolean> task=()->{start.await();Booking request=new Booking();request.setSeatCount(1);
     try{bookingService.createBooking(uid,mid,request);return true;}
     catch(com.examly.springapp.exception.BookingNotFoundException expected){return false;}};
   try {
     var one=pool.submit(task);var two=pool.submit(task);start.countDown();
     int success=(one.get(10,java.util.concurrent.TimeUnit.SECONDS)?1:0)+(two.get(10,java.util.concurrent.TimeUnit.SECONDS)?1:0);
     org.junit.jupiter.api.Assertions.assertEquals(1,success);
     org.junit.jupiter.api.Assertions.assertEquals(1,bookings.countBookedSeatsByMovie(mid));
   } finally {pool.shutdownNow();}
 }

}
