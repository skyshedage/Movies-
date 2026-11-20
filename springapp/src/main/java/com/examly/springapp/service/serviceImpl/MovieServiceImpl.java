package com.examly.springapp.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.examly.springapp.entity.Movie;
import com.examly.springapp.repository.MovieRepo;
import com.examly.springapp.service.MovieService;

@Service
public class MovieServiceImpl implements MovieService {
    @Autowired private MovieRepo repo;
    @Autowired private com.examly.springapp.repository.BookingRepo bookings;
    private void validate(Movie m){
        if(m.getTitle()==null || m.getTitle().isBlank() || m.getDuration()<=0 || m.getPrice()<0 || m.getTotalSeats()<=0)
            throw new IllegalArgumentException("Title, positive duration/capacity and nonnegative price are required");
    }

    @Override
    public Movie addMovie(Movie m) {
        validate(m);m.setId(0);return repo.save(m);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Movie updateMovie(Long id, Movie m) {
        validate(m);
        Movie l = repo.findForBooking(id).orElse(null);
        if(m.getTotalSeats()<bookings.countBookedSeatsByMovie(id)) throw new IllegalArgumentException("Capacity cannot be below booked seats");
        if (l == null) {
            return null;
        }

        l.setId(id);
        l.setTitle(m.getTitle());
        l.setDuration(m.getDuration());
        l.setGenre(m.getGenre());
        l.setPrice(m.getPrice());
        l.setTotalSeats(m.getTotalSeats());
        return repo.save(l);
    }

    @Override
    public List<Movie> getAllMovies() {
        return repo.findAll();
    }

    @Override
    public Movie getMovieById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public boolean deleteMovie(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }

}