package com.moveit.championship.strategy;

import com.moveit.championship.entity.CompetitionType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory qui résout la bonne stratégie de génération d'arbre
 * en fonction du type de compétition.
 * Utilise un registry : chaque stratégie déclare son type via {@link TreeGenerationStrategy#getType()},
 * la factory les collecte automatiquement au démarrage.
 */
@Component
public class TreeStrategyFactory {

    private final Map<CompetitionType, TreeGenerationStrategy> strategyRegistry;

    /**
     * Construit le registry à partir de toutes les implémentations
     * de {@link TreeGenerationStrategy} injectées par Spring.
     *
     * @param strategies la liste de toutes les stratégies disponibles
     */
    public TreeStrategyFactory(List<TreeGenerationStrategy> strategies) {
        this.strategyRegistry = strategies.stream()
                .collect(Collectors.toMap(TreeGenerationStrategy::getType, Function.identity()));
    }

    /**
     * Retourne la stratégie correspondant au type de compétition.
     *
     * @param type le type de compétition
     * @return la stratégie adaptée
     * @throws IllegalArgumentException si aucune stratégie n'est enregistrée pour ce type
     */
    public TreeGenerationStrategy getStrategy(CompetitionType type) {
        TreeGenerationStrategy strategy = strategyRegistry.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Aucune stratégie enregistrée pour le type : " + type);
        }
        return strategy;
    }
}
