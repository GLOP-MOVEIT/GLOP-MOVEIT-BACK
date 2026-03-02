package com.moveit.championship.strategy;

import com.moveit.championship.entity.CompetitionType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class TreeStrategyFactory {

    private final Map<CompetitionType, TreeGenerationStrategy> strategyRegistry;


    public TreeStrategyFactory(List<TreeGenerationStrategy> strategies) {
        this.strategyRegistry = strategies.stream()
                .collect(Collectors.toMap(TreeGenerationStrategy::getType, Function.identity()));
    }


    public TreeGenerationStrategy getStrategy(CompetitionType type) {
        TreeGenerationStrategy strategy = strategyRegistry.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Aucune stratégie enregistrée pour le type : " + type);
        }
        return strategy;
    }
}
