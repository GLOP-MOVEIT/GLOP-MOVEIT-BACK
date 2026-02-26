package com.moveit.location.exception;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(Integer id) {
        super("Lieu non trouvé avec l'ID : " + id);
    }
}
