package com.moveit.championship.strategy;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.CompetitionType;
import com.moveit.championship.entity.Trial;

import java.util.List;

/**
 * Interface Strategy pour la génération d'arbres de compétition.
 * Chaque implémentation définit une logique différente de génération
 * des manches (trials) pour une compétition donnée.
 */
public interface TreeGenerationStrategy {

    /**
     * Retourne le type de compétition géré par cette stratégie.
     *
     * @return le CompetitionType associé
     */
    CompetitionType getType();

    /**
     * Génère la liste des manches (trials) pour une compétition.
     *
     * @param competition   la compétition pour laquelle générer l'arbre
     * @param nbParticipants le nombre de participants
     * @return la liste des trials générés
     */
    List<Trial> generateTrials(Competition competition, int nbParticipants);
}
