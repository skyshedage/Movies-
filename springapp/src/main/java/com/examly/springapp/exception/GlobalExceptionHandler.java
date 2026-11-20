package com.examly.springapp.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badInput(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    @ExceptionHandler(value = InsufficientSeatCountException.class)
    public ResponseEntity<String> handleException(Exception e){
        return ResponseEntity.status(404).body(e.getMessage());
    }
    @ExceptionHandler(value=BookingNotFoundException.class)
    public ResponseEntity<String> handleExcpetionsecond(Exception e){
        return ResponseEntity.status(404).body(e.getMessage());
    }
    @ExceptionHandler(value = MovieNotFoundException.class)
    public ResponseEntity<String> handleExceptionThird(Exception e){
        return ResponseEntity.status(404).body(e.getMessage());
    }
    @ExceptionHandler(value = UserNotFoundException.class)
    public ResponseEntity<String> handleExceptionFourth(Exception e){
        return ResponseEntity.status(404).body(e.getMessage());
    }
    

}
