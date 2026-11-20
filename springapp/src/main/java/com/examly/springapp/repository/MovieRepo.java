package com.examly.springapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.examly.springapp.entity.Movie;

@Repository
public interface MovieRepo extends JpaRepository<Movie,Long>{
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select m from Movie m where m.id = :id")
    java.util.Optional<Movie> findForBooking(@org.springframework.data.repository.query.Param("id") Long id);

}
