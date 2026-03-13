package com.moveit.volunteer_service.repository;

import com.moveit.volunteer_service.entity.VolunteerPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface VolunteerPreferenceRepository extends JpaRepository<VolunteerPreference, Long> {
	List<VolunteerPreference> findByUserIdOrderByPreferenceOrder(Long userId);
	Optional<VolunteerPreference> findByUserIdAndTaskType_Id(Long userId, Long taskTypeId);
	List<VolunteerPreference> findByTaskType_IdAndUserIdIn(Long taskTypeId, List<Long> userIds);
	Optional<VolunteerPreference> findByUserIdAndPreferenceOrder(Long userId, Integer preferenceOrder);
	Optional<VolunteerPreference> findByUserIdAndPreferenceOrderAndIdNot(Long userId, Integer preferenceOrder, Long id);
}
