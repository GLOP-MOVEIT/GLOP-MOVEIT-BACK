package com.moveit.championship.repository;

import com.moveit.championship.entity.Trial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrialRepository extends JpaRepository<Trial, Integer> {
    List<Trial> findByCompetition_CompetitionId(Integer competitionId);

    @Query("select distinct t from Trial t join t.participantIds participantId where participantId in :participantIds")
    List<Trial> findByParticipantIds(@Param("participantIds") List<Integer> participantIds);
}
