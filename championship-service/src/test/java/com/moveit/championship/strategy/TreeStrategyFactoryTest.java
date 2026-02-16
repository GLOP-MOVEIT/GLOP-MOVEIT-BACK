package com.moveit.championship.strategy;

import com.moveit.championship.entity.CompetitionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreeStrategyFactoryTest {

    @Mock
    private SingleEliminationStrategy singleEliminationStrategy;
    @Mock
    private RoundRobinStrategy roundRobinStrategy;
    @Mock
    private HeatsStrategy heatsStrategy;

    private TreeStrategyFactory factory;

    @BeforeEach
    void setUp() {
        when(singleEliminationStrategy.getType()).thenReturn(CompetitionType.SINGLE_ELIMINATION);
        when(roundRobinStrategy.getType()).thenReturn(CompetitionType.ROUND_ROBIN);
        when(heatsStrategy.getType()).thenReturn(CompetitionType.HEATS);
        factory = new TreeStrategyFactory(List.of(singleEliminationStrategy, roundRobinStrategy, heatsStrategy));
    }

    @Test
    @DisplayName("Should return SingleEliminationStrategy for SINGLE_ELIMINATION type")
    void testGetStrategy_SingleElimination() {
        TreeGenerationStrategy strategy = factory.getStrategy(CompetitionType.SINGLE_ELIMINATION);
        assertThat(strategy).isEqualTo(singleEliminationStrategy);
    }

    @Test
    @DisplayName("Should return RoundRobinStrategy for ROUND_ROBIN type")
    void testGetStrategy_RoundRobin() {
        TreeGenerationStrategy strategy = factory.getStrategy(CompetitionType.ROUND_ROBIN);
        assertThat(strategy).isEqualTo(roundRobinStrategy);
    }

    @Test
    @DisplayName("Should return HeatsStrategy for HEATS type")
    void testGetStrategy_Heats() {
        TreeGenerationStrategy strategy = factory.getStrategy(CompetitionType.HEATS);
        assertThat(strategy).isEqualTo(heatsStrategy);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for unknown type")
    void testGetStrategy_UnknownType_ThrowsException() {
        TreeStrategyFactory partialFactory = new TreeStrategyFactory(List.of(singleEliminationStrategy));
        assertThatThrownBy(() -> partialFactory.getStrategy(CompetitionType.ROUND_ROBIN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Aucune stratégie enregistrée pour le type");
    }
}
