package com.moveit.volunteer_service.service;

import com.moveit.volunteer_service.dto.CreateVolunteerTaskRequest;
import com.moveit.volunteer_service.entity.VolunteerTask;
import com.moveit.volunteer_service.entity.VolunteerTaskType;
import com.moveit.volunteer_service.enums.TaskStatus;
import com.moveit.volunteer_service.exception.VolunteerTaskNotFoundException;
import com.moveit.volunteer_service.exception.VolunteerTaskTypeNotFoundException;
import com.moveit.volunteer_service.repository.VolunteerTaskRepository;
import com.moveit.volunteer_service.repository.VolunteerTaskTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VolunteerTaskService {

    private final VolunteerTaskRepository volunteerTaskRepository;
    private final VolunteerTaskTypeRepository volunteerTaskTypeRepository;

    public List<VolunteerTask> getAllTasks() {
        return volunteerTaskRepository.findAll();
    }

    public VolunteerTask getTaskById(Long id) {
        return volunteerTaskRepository.findById(id)
                .orElseThrow(() -> new VolunteerTaskNotFoundException(id));
    }

    public List<VolunteerTask> getTasksByChampionshipId(Long championshipId) {
        return volunteerTaskRepository.findByChampionshipId(championshipId);
    }

    public List<VolunteerTask> getTasksByTaskTypeId(Long taskTypeId) {
        return volunteerTaskRepository.findByTaskType_Id(taskTypeId);
    }

    public VolunteerTask createTask(CreateVolunteerTaskRequest request) {
        VolunteerTaskType taskType = volunteerTaskTypeRepository.findById(request.getTaskTypeId())
                .orElseThrow(() -> new VolunteerTaskTypeNotFoundException(request.getTaskTypeId()));

        VolunteerTask task = new VolunteerTask();
        task.setChampionshipId(request.getEventlId());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setTaskType(taskType);
        task.setStatus(TaskStatus.PENDING);
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());
        task.setMaxVolunteers(request.getMaxVolunteers());
        task.setLocation(request.getLocation());
        return volunteerTaskRepository.save(task);
    }

    public VolunteerTask updateTask(Long id, CreateVolunteerTaskRequest request) {
        VolunteerTask existing = volunteerTaskRepository.findById(id)
                .orElseThrow(() -> new VolunteerTaskNotFoundException(id));
        VolunteerTaskType taskType = volunteerTaskTypeRepository.findById(request.getTaskTypeId())
                .orElseThrow(() -> new VolunteerTaskTypeNotFoundException(request.getTaskTypeId()));

        existing.setChampionshipId(request.getEventlId());
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setTaskType(taskType);
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setMaxVolunteers(request.getMaxVolunteers());
        existing.setLocation(request.getLocation());
        return volunteerTaskRepository.save(existing);
    }

    public VolunteerTask updateTaskStatus(Long id, TaskStatus status) {
        VolunteerTask existing = volunteerTaskRepository.findById(id)
                .orElseThrow(() -> new VolunteerTaskNotFoundException(id));
        existing.setStatus(status);
        return volunteerTaskRepository.save(existing);
    }

    public void deleteTask(Long id) {
        if (!volunteerTaskRepository.existsById(id)) {
            throw new VolunteerTaskNotFoundException(id);
        }
        volunteerTaskRepository.deleteById(id);
    }
}
