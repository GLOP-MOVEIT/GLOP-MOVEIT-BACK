package com.moveit.result.service;

import com.moveit.result.dto.Result;
import com.moveit.result.exception.ResultNotFoundException;
import com.moveit.result.mapper.ResultMapper;
import com.moveit.result.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    private final ResultMapper resultMapper;

    public Result saveResult(Result result) {
        var resultEntity = this.resultMapper.toEntity(result);
        var savedEntity = this.resultRepository.save(resultEntity);
        return this.resultMapper.toDto(savedEntity);
    }

    public Result getResultByTrialId(Integer trialId) {
        var resultEntity = this.resultRepository.findByTrialId(trialId)
                .orElseThrow(() -> new ResultNotFoundException("Result not found for trialId: " + trialId));
        return this.resultMapper.toDto(resultEntity);
    }

    public Result updateResult(Result result) {
        var existingResultEntity = this.resultRepository.findByTrialId(result.getTrialId())
                .orElseThrow(() -> new ResultNotFoundException("Result not found for trialId: " + result.getTrialId()));

        existingResultEntity.setTrialId(result.getTrialId());
        existingResultEntity.setLastTrial(Boolean.TRUE.equals(result.getLastTrial()));
        existingResultEntity.setRankings(result.getRankings());

        var updatedEntity = this.resultRepository.save(existingResultEntity);
        return this.resultMapper.toDto(updatedEntity);
    }

    public List<Result> getAllResultsByParticipantId(Integer participantId) {
        var resultEntities = this.resultRepository.findByParticipantsId(participantId);
        return resultEntities.stream().map(this.resultMapper::toDto).toList();
    }
}