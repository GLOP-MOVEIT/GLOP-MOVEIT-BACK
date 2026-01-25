package com.moveit.localization.repository;

import com.moveit.localization.entity.LocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {

    List<LocationHistory> findByUserIdOrderByTimestampDesc(Integer userId);

    List<LocationHistory> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            Integer userId,
            LocalDateTime start,
            LocalDateTime end
    );

    // Obtenir l'historique récent d'un utilisateur
    @Query("SELECT lh FROM LocationHistory lh WHERE lh.userId = :userId AND lh.timestamp >= :since ORDER BY lh.timestamp DESC")
    List<LocationHistory> findRecentHistory(@Param("userId") Integer userId, @Param("since") LocalDateTime since);

    // Nettoyer l'ancien historique
    void deleteByTimestampBefore(LocalDateTime before);

}
