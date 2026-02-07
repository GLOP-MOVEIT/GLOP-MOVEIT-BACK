package com.moveit.championship.exception;

public class TrialNotFoundException extends RuntimeException {
    public TrialNotFoundException(Integer id) {
        super("Manche non trouvée avec l'ID: " + id);
    }
}
