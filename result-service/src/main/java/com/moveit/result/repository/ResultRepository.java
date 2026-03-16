package com.moveit.result.repository;

import com.moveit.result.entity.ResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<ResultEntity, Integer> {

    Optional<ResultEntity> findByTrialId(Integer trialId);

    @Query("SELECT r FROM ResultEntity r JOIN r.rankings rk WHERE rk.id = :rankingId")
    List<ResultEntity> findByParticipantsId(@Param("rankingId") Integer rankingId);
}