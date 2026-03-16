package com.moveit.volunteer_service.service;

import com.moveit.volunteer_service.dto.CreateVolunteerTaskTypeRequest;
import com.moveit.volunteer_service.entity.VolunteerTaskType;
import com.moveit.volunteer_service.exception.VolunteerTaskTypeNotFoundException;
import com.moveit.volunteer_service.repository.VolunteerTaskTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VolunteerTaskTypeService {

    private final VolunteerTaskTypeRepository volunteerTaskTypeRepository;

    public List<VolunteerTaskType> getAllTaskTypes() {
        return volunteerTaskTypeRepository.findAll();
    }

    public VolunteerTaskType getTaskTypeById(Long id) {
        return volunteerTaskTypeRepository.findById(id)
                .orElseThrow(() -> new VolunteerTaskTypeNotFoundException(id));
    }

    public VolunteerTaskType createTaskType(CreateVolunteerTaskTypeRequest request) {
        VolunteerTaskType taskType = new VolunteerTaskType();
        taskType.setName(request.getName());
        taskType.setDescription(request.getDescription());
        return volunteerTaskTypeRepository.save(taskType);
    }

    public VolunteerTaskType updateTaskType(Long id, CreateVolunteerTaskTypeRequest request) {
        VolunteerTaskType existing = volunteerTaskTypeRepository.findById(id)
                .orElseThrow(() -> new VolunteerTaskTypeNotFoundException(id));
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        return volunteerTaskTypeRepository.save(existing);
    }

    public void deleteTaskType(Long id) {
        if (!volunteerTaskTypeRepository.existsById(id)) {
            throw new VolunteerTaskTypeNotFoundException(id);
        }
        volunteerTaskTypeRepository.deleteById(id);
    }
}
