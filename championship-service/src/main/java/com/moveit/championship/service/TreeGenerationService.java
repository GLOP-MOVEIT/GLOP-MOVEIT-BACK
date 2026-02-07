package com.moveit.championship.service;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.Trial;
import com.moveit.championship.exception.CompetitionNotFoundException;
import com.moveit.championship.repository.CompetitionRepository;
import com.moveit.championship.repository.TrialRepository;
import com.moveit.championship.strategy.TreeGenerationStrategy;
import com.moveit.championship.strategy.TreeStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsable de la génération de l'arbre complet d'une compétition.
 * Utilise le pattern Strategy via la factory pour choisir la bonne logique
 * de génération selon le type de compétition.
 */
@Service
@RequiredArgsConstructor
public class TreeGenerationService {

    private final CompetitionRepository competitionRepository;
    private final TrialRepository trialRepository;
    private final TreeStrategyFactory treeStrategyFactory;

    /**
     * Génère l'arbre complet (les trials) d'une compétition.
     * Supprime les anciens trials avant de générer les nouveaux.
     *
     * @param competitionId  l'ID de la compétition
     * @param nbParticipants le nombre de participants
     * @return la compétition avec ses trials générés
     */
    @Transactional
    public Competition generateTree(Integer competitionId, int nbParticipants) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException(competitionId));

        // Supprime les anciens trials
        competition.getTrials().clear();
        competitionRepository.save(competition);

        // Récupère la stratégie en fonction du type de compétition
        TreeGenerationStrategy strategy = treeStrategyFactory.getStrategy(competition.getCompetitionType());

        // Génère les nouveaux trials
        List<Trial> generatedTrials = strategy.generateTrials(competition, nbParticipants);

        // Associe et sauvegarde
        competition.getTrials().addAll(generatedTrials);
        competition.setNbManches(generatedTrials.size());

        return competitionRepository.save(competition);
    }
}
