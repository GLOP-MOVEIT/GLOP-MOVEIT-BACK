package com.moveit.championship.service;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.Trial;
import com.moveit.championship.exception.CompetitionNotFoundException;
import com.moveit.championship.exception.TrialNotFoundException;
import com.moveit.championship.repository.CompetitionRepository;
import com.moveit.championship.repository.TrialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrialService {

    private final TrialRepository trialRepository;
    private final CompetitionRepository competitionRepository;


    public Trial getTrialById(Integer id) {
        return trialRepository.findById(id)
                .orElseThrow(() -> new TrialNotFoundException(id));
    }

    public List<Trial> getTrialsByCompetitionId(Integer competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            throw new CompetitionNotFoundException(competitionId);
        }
        return trialRepository.findByCompetition_CompetitionId(competitionId);
    }

    @Transactional
    public Trial createTrial(Integer competitionId, Trial trial) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException(competitionId));
        
        trial.setCompetition(competition);
        return trialRepository.save(trial);
    }

    @Transactional
    public Trial updateTrial(Integer id, Trial trial) {
        Trial existingTrial = trialRepository.findById(id)
                .orElseThrow(() -> new TrialNotFoundException(id));


        existingTrial.setTrialName(trial.getTrialName());
        existingTrial.setTrialStartDate(trial.getTrialStartDate());
        existingTrial.setTrialEndDate(trial.getTrialEndDate());
        existingTrial.setTrialDescription(trial.getTrialDescription());
        existingTrial.setTrialStatus(trial.getTrialStatus());
        existingTrial.setLocation(trial.getLocation());
        existingTrial.setRoundNumber(trial.getRoundNumber());
        existingTrial.setPosition(trial.getPosition());

        return trialRepository.save(existingTrial);
    }

    @Transactional
    public void deleteTrial(Integer id) {
        if (!trialRepository.existsById(id)) {
            throw new TrialNotFoundException(id);
        }
        trialRepository.deleteById(id);
    }
}
