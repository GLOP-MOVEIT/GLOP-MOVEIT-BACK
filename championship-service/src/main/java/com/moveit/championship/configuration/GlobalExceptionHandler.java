package com.moveit.championship.configuration;

import com.moveit.championship.exception.ChampionshipNotFoundException;
import com.moveit.championship.exception.CompetitionNotFoundException;
import com.moveit.championship.exception.LocationNotFoundException;
import com.moveit.championship.exception.TrialNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ChampionshipNotFoundException.class)
    public ResponseEntity<Void> handleChampionshipNotFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CompetitionNotFoundException.class)
    public ResponseEntity<Void> handleCompetitionNotFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TrialNotFoundException.class)
    public ResponseEntity<Void> handleTrialNotFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<Void> handleLocationNotFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}