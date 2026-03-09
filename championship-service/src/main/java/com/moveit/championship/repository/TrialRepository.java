package com.moveit.championship.repository;

import com.moveit.championship.entity.Trial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrialRepository extends JpaRepository<Trial, Integer> {
    List<Trial> findByCompetition_CompetitionId(Integer competitionId);
}
