package com.moveit.volunteer_service.repository;

import com.moveit.volunteer_service.entity.VolunteerTaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface VolunteerTaskTypeRepository extends JpaRepository<VolunteerTaskType, Long> {
	Optional<VolunteerTaskType> findByName(String name);
}
