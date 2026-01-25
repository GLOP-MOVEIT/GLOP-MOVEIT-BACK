package com.moveit.localization.repository;

import com.moveit.localization.entity.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {

    Optional<UserLocation> findByUserId(Integer userId);

    // Trouver les utilisateurs proches d'un point (utilisant la formule haversine)
    @Query(value = """
        SELECT * FROM user_locations u
        WHERE (6371 * acos(
            cos(radians(:latitude)) * cos(radians(u.latitude)) *
            cos(radians(u.longitude) - radians(:longitude)) +
            sin(radians(:latitude)) * sin(radians(u.latitude))
        )) <= :radiusKm
        AND u.user_id != :excludeUserId
        ORDER BY (6371 * acos(
            cos(radians(:latitude)) * cos(radians(u.latitude)) *
            cos(radians(u.longitude) - radians(:longitude)) +
            sin(radians(:latitude)) * sin(radians(u.latitude))
        ))
        """, nativeQuery = true)
    List<UserLocation> findNearbyUsers(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm,
            @Param("excludeUserId") Integer excludeUserId
    );
}
