package com.moveit.championship.service;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.Status;
import com.moveit.championship.exception.CompetitionNotFoundException;
import com.moveit.championship.repository.CompetitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitionService {

    private final CompetitionRepository competitionRepository;

    public List<Competition> getAllCompetitions() {
        return competitionRepository.findAll();
    }

    public Competition getCompetitionById(Integer id) {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));
    }

    public Competition createCompetition(Competition competition) {
        attachEvents(competition);
        applyDefaultStatus(competition);
        return competitionRepository.save(competition);
    }

    public Competition updateCompetition(Integer id, Competition competition) {
        competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));
        competition.setCompetitionId(id);
        attachEvents(competition);
        applyDefaultStatus(competition);
        return competitionRepository.save(competition);
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
}
