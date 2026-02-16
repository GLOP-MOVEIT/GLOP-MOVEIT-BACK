package com.moveit.championship.service;

import com.moveit.championship.entity.Championship;
import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.Status;
import com.moveit.championship.exception.ChampionshipNotFoundException;
import com.moveit.championship.exception.CompetitionNotFoundException;
import com.moveit.championship.repository.ChampionshipRepository;
import com.moveit.championship.repository.CompetitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final ChampionshipRepository championshipRepository;

    public List<Competition> getAllCompetitions() {
        return competitionRepository.findAll();
    }

    public Competition getCompetitionById(Integer id) {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));
    }

    public Competition createCompetition(Competition competition) {
        if (competition.getChampionship() == null || competition.getChampionship().getId() == null) {
            throw new IllegalArgumentException("Championship ID is required");
        }

        var championship = championshipRepository.findById(competition.getChampionship().getId())
                .orElseThrow(() -> new ChampionshipNotFoundException(competition.getChampionship().getId()));

        validateCompetitionDates(competition, championship);
        attachEvents(competition);
        applyDefaultStatus(competition);
        return competitionRepository.save(competition);
    }

    public Competition updateCompetition(Integer id, Competition competition) {
        competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));

        var championship = championshipRepository.findById(competition.getChampionship().getId())
                .orElseThrow(() -> new ChampionshipNotFoundException(competition.getChampionship().getId()));

        validateCompetitionDates(competition, championship);
        competition.setCompetitionId(id);
        attachEvents(competition);
        applyDefaultStatus(competition);
        return competitionRepository.save(competition);
    }

    public Competition updateCompetition(Integer id, com.moveit.championship.dto.CompetitionUpdateDTO dto) {
        Competition existing = competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));

        existing.setCompetitionName(dto.getCompetitionName());
        existing.setCompetitionStartDate(dto.getCompetitionStartDate());
        existing.setCompetitionEndDate(dto.getCompetitionEndDate());
        existing.setCompetitionDescription(dto.getCompetitionDescription());
        existing.setCompetitionStatus(dto.getCompetitionStatus());

        validateCompetitionDates(existing, existing.getChampionship());
        applyDefaultStatus(existing);
        return competitionRepository.save(existing);
    }

    public void deleteCompetition(Integer id) {
        if (!competitionRepository.existsById(id)) {
            throw new CompetitionNotFoundException(id);
        }
        competitionRepository.deleteById(id);
    }

    private void attachEvents(Competition competition) {
        if (competition.getEvents() != null) {
            competition.getEvents().forEach(event -> event.setCompetition(competition));
        }
    }

    private void applyDefaultStatus(Competition competition) {
        if (competition.getCompetitionStatus() == null) {
            competition.setCompetitionStatus(Status.PLANNED);
        }
    }

    private void validateCompetitionDates(Competition competition, Championship championship) {
        if (competition.getCompetitionStartDate().after(competition.getCompetitionEndDate())) {
            throw new IllegalArgumentException("La date de début de la compétition doit être avant la date de fin");
        }
        if (competition.getCompetitionStartDate().before(championship.getStartDate())) {
            throw new IllegalArgumentException("La date de début de la compétition doit être après ou égale à la date de début du championnat");
        }
        if (competition.getCompetitionEndDate().after(championship.getEndDate())) {
            throw new IllegalArgumentException("La date de fin de la compétition doit être avant ou égale à la date de fin du championnat");
        }
    }
}