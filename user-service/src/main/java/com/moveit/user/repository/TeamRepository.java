package com.moveit.user.repository;

import com.moveit.user.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<TeamEntity, Integer> {
    List<TeamEntity> findDistinctByAthletes_UserId(Integer athleteId);
}
