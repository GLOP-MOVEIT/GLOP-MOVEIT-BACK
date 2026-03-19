package com.moveit.championship.repository;

import com.moveit.championship.entity.Trial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrialRepository extends JpaRepository<Trial, Integer> {
    List<Trial> findByCompetition_CompetitionId(Integer competitionId);

    @Query("select distinct t from Trial t join t.participantIds participantId where participantId in :participantIds")
    List<Trial> findByParticipantIds(@Param("participantIds") List<Integer> participantIds);

        @Query("""
                        select distinct t
                        from Trial t
                        join t.participantIds participantId
                        where t.trialId <> :excludedTrialId
                            and participantId in :participantIds
                            and t.trialStartDate < :candidateEnd
                            and t.trialEndDate > :candidateStart
                        """)
        List<Trial> findConflictingTrials(@Param("excludedTrialId") Integer excludedTrialId,
                                                                            @Param("participantIds") List<Integer> participantIds,
                                                                            @Param("candidateStart") LocalDateTime candidateStart,
                                                                            @Param("candidateEnd") LocalDateTime candidateEnd);
}
