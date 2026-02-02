package com.moveit.volunteer_service.repository;

import com.moveit.volunteer_service.entity.VolunteerTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface VolunteerTaskRepository extends JpaRepository<VolunteerTask, Long> {
	List<VolunteerTask> findByTaskType_Id(Long taskTypeId);
	List<VolunteerTask> findByStartDateBetween(LocalDateTime start, LocalDateTime end);
	List<VolunteerTask> findByChampionshipId(Long championshipId);
}
