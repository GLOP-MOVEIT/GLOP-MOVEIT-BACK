package com.moveit.championship.service;

import com.moveit.championship.dto.ChampionshipUpdateDTO;
import com.moveit.championship.entity.Championship;
import com.moveit.championship.exception.ChampionshipNotFoundException;
import com.moveit.championship.repository.ChampionshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChampionshipService {

    private final ChampionshipRepository championshipRepository;

    public List<Championship> getAllChampionships() {
        return championshipRepository.findAll();
    }

    public Championship getChampionshipById(Integer id) {
        return championshipRepository.findById(id)
                .orElseThrow(() -> new ChampionshipNotFoundException(id));
    }

    public Championship createChampionship(Championship championship) {
        return championshipRepository.save(championship);
    }

    public Championship updateChampionship(Integer id, ChampionshipUpdateDTO dto) {
        Championship existing = championshipRepository.findById(id)
                .orElseThrow(() -> new ChampionshipNotFoundException(id));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }

        validateChampionshipDates(existing);
        return championshipRepository.save(existing);
    }

    private void validateChampionshipDates(Championship championship) {
        if (championship.getStartDate().after(championship.getEndDate())) {
            throw new IllegalArgumentException("La date de début du championnat doit être avant la date de fin");
        }
    }

    public void deleteChampionship(Integer id) {
        if (!championshipRepository.existsById(id)) {
            throw new ChampionshipNotFoundException(id);
        }
        championshipRepository.deleteById(id);
    }
}