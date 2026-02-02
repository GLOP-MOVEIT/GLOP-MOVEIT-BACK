package com.moveit.volunteer_service.repository;

import com.moveit.volunteer_service.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.moveit.volunteer_service.enums.AssignmentStatus;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
	List<TaskAssignment> findByVolunteerId(Long volunteerId);
	List<TaskAssignment> findByTaskId(Long taskId);
	List<TaskAssignment> findByStatus(AssignmentStatus status);
	Optional<TaskAssignment> findByVolunteerIdAndTaskId(Long volunteerId, Long taskId);
}
