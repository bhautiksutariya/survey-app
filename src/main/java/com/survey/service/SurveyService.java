package com.survey.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.survey.dto.*;
import com.survey.model.*;
import com.survey.repository.*;
import com.survey.security.UserPrincipal;
import com.survey.util.AppConstants;
import com.survey.util.ModelMapper;

import com.survey.exception.BadRequestException;
import com.survey.exception.ResourceNotFoundException;

import javax.transaction.Transactional;

@Service
public class SurveyService {

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(SurveyService.class);

    public PageableResponse<SurveyResponseDTO> getAllSurveys(UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        // Retrieve Surveys
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Survey> surveys = surveyRepository.findAll(pageable);

        if(surveys.getNumberOfElements() == 0) {
            return new PageableResponse<>(Collections.emptyList(), surveys.getNumber(),
                    surveys.getSize(), surveys.getTotalElements(), surveys.getTotalPages(), surveys.isLast());
        }

        // Map Surveys to SurveyResponses containing vote counts and survey creator details
        List<Long> surveyIds = surveys.map(Survey::getId).getContent();
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(surveyIds);
        Map<Long, Long> surveyUserVoteMap = getSurveyUserVoteMap(currentUser, surveyIds);
        Map<Long, User> creatorMap = getSurveyCreatorMap(surveys.getContent());

        List<SurveyResponseDTO> surveyResponses = surveys.map(survey -> {
            return ModelMapper.mapSurveyToSurveyResponse(survey,
                    choiceVoteCountMap,
                    creatorMap.get(survey.getCreatedBy()),
                    surveyUserVoteMap == null ? null : surveyUserVoteMap.getOrDefault(survey.getId(), null));
        }).getContent();

        return new PageableResponse<>(surveyResponses, surveys.getNumber(),
                surveys.getSize(), surveys.getTotalElements(), surveys.getTotalPages(), surveys.isLast());
    }

    public PageableResponse<SurveyResponseDTO> getSurveysCreatedBy(String username, UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Retrieve all surveys created by the given username
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Survey> surveys = surveyRepository.findByCreatedBy(user.getId(), pageable);

        if (surveys.getNumberOfElements() == 0) {
            return new PageableResponse<>(Collections.emptyList(), surveys.getNumber(),
                    surveys.getSize(), surveys.getTotalElements(), surveys.getTotalPages(), surveys.isLast());
        }

        // Map Surveys to SurveyResponses containing vote counts and survey creator details
        List<Long> surveyIds = surveys.map(Survey::getId).getContent();
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(surveyIds);
        Map<Long, Long> surveyUserVoteMap = getSurveyUserVoteMap(currentUser, surveyIds);

        List<SurveyResponseDTO> surveyResponses = surveys.map(survey -> {
            return ModelMapper.mapSurveyToSurveyResponse(survey,
                    choiceVoteCountMap,
                    user,
                    surveyUserVoteMap == null ? null : surveyUserVoteMap.getOrDefault(survey.getId(), null));
        }).getContent();

        return new PageableResponse<>(surveyResponses, surveys.getNumber(),
                surveys.getSize(), surveys.getTotalElements(), surveys.getTotalPages(), surveys.isLast());
    }

    public PageableResponse<SurveyResponseDTO> getSurveysVotedBy(String username, UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Retrieve all surveyIds in which the given username has voted
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Long> userVotedSurveyIds = voteRepository.findVotedSurveyIdsByUserId(user.getId(), pageable);

        if (userVotedSurveyIds.getNumberOfElements() == 0) {
            return new PageableResponse<>(Collections.emptyList(), userVotedSurveyIds.getNumber(),
                    userVotedSurveyIds.getSize(), userVotedSurveyIds.getTotalElements(),
                    userVotedSurveyIds.getTotalPages(), userVotedSurveyIds.isLast());
        }

        // Retrieve all survey details from the voted surveyIds.
        List<Long> surveyIds = userVotedSurveyIds.getContent();

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Survey> surveys = surveyRepository.findByIdIn(surveyIds, sort);

        // Map Surveys to SurveyResponses containing vote counts and survey creator details
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(surveyIds);
        Map<Long, Long> surveyUserVoteMap = getSurveyUserVoteMap(currentUser, surveyIds);
        Map<Long, User> creatorMap = getSurveyCreatorMap(surveys);

        List<SurveyResponseDTO> surveyResponses = surveys.stream().map(survey -> {
            return ModelMapper.mapSurveyToSurveyResponse(survey,
                    choiceVoteCountMap,
                    creatorMap.get(survey.getCreatedBy()),
                    surveyUserVoteMap == null ? null : surveyUserVoteMap.getOrDefault(survey.getId(), null));
        }).collect(Collectors.toList());

        return new PageableResponse<>(surveyResponses, userVotedSurveyIds.getNumber(), userVotedSurveyIds.getSize(), userVotedSurveyIds.getTotalElements(), userVotedSurveyIds.getTotalPages(), userVotedSurveyIds.isLast());
    }


    public Survey createSurvey(SurveyRequest surveyRequest) {
        Survey survey = new Survey();
        survey.setQuestion(surveyRequest.getQuestion());

        surveyRequest.getChoices().forEach(choiceRequest -> {
            survey.addChoice(new Choice(choiceRequest.getText()));
        });
        
        Instant now = Instant.now();
        Instant expirationDateTime = now.plus(Duration.ofHours(surveyRequest.getSurveyExpiry().getHours()));

        survey.setExpirationDateTime(expirationDateTime);

        return surveyRepository.save(survey);
    }

    @Transactional
    public Survey updateSurvey(SurveyUpdateRequest surveyUpdateRequest) {
    	
    	Survey getSurvey = surveyRepository.findById(surveyUpdateRequest.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Survey", "id", surveyUpdateRequest.getId()));
    	
    	List<Long> choiceIds = getSurvey.getChoices().
    			stream().map(choice -> choice.getId()).
    			collect(Collectors.toList());

        voteRepository.deleteByChoice_IdIn(choiceIds);
    	
    	Survey survey = new Survey();
        
        survey.setId(surveyUpdateRequest.getId());
        
        survey.setQuestion(surveyUpdateRequest.getQuestion());

        surveyUpdateRequest.getChoices().forEach(choiceRequest -> {
            survey.addChoice(new Choice(choiceRequest.getText()));
        });
        
        Instant now = Instant.now();
        Instant expirationDateTime = now.plus(Duration.ofHours(surveyUpdateRequest.getSurveyExpiry().getHours()));

        survey.setExpirationDateTime(expirationDateTime);

        return surveyRepository.save(survey);
    }

    @Transactional
    public void deleteSurvey(Long surveyId) {
    	Survey survey = surveyRepository.findById(surveyId).orElseThrow(
                () -> new ResourceNotFoundException("Survey", "id", surveyId));
    	
    	List<Long> choiceIds = survey.getChoices().
    			stream().map(Choice::getId).
    			collect(Collectors.toList());

    	System.out.println("ids"+choiceIds);
    	
    	voteRepository.deleteByChoice_IdIn(choiceIds);
    	
    	survey.getChoices().clear();
    	
    	surveyRepository.deleteById(surveyId);
    }

    public SurveyResponseDTO getSurveyById(Long surveyId, UserPrincipal currentUser) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(
                () -> new ResourceNotFoundException("Survey", "id", surveyId));

        // Retrieve Vote Counts of every choice belonging to the current survey
        List<ChoiceVoteCount> votes = voteRepository.countBySurveyIdGroupByChoiceId(surveyId);

        Map<Long, Long> choiceVotesMap = votes.stream()
                .collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

        // Retrieve survey creator details
        User creator = userRepository.findById(survey.getCreatedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", survey.getCreatedBy()));

        // Retrieve vote done by logged in user
        Vote userVote = null;
        if(currentUser != null) {
            userVote = voteRepository.findByUserIdAndSurveyId(currentUser.getId(), surveyId);
        }

        return ModelMapper.mapSurveyToSurveyResponse(survey, choiceVotesMap,
                creator, userVote != null ? userVote.getChoice().getId(): null);
    }

    public SurveyResponseDTO castVoteAndGetUpdatedSurvey(Long surveyId, Long voteRequest, UserPrincipal currentUser) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey", "id", surveyId));

        if(survey.getExpirationDateTime().isBefore(Instant.now())) {
            throw new BadRequestException("Sorry! This Survey has already expired");
        }

        User user = userRepository.getOne(currentUser.getId());

        Choice selectedChoice = survey.getChoices().stream()
                .filter(choice -> choice.getId().equals(voteRequest))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Choice", "id", voteRequest));

        Vote vote = new Vote();
        vote.setSurvey(survey);
        vote.setUser(user);
        vote.setChoice(selectedChoice);

        try {
            vote = voteRepository.save(vote);
        } catch (DataIntegrityViolationException ex) {
            logger.info("User {} has already voted in Survey {}", currentUser.getId(), surveyId);
            throw new BadRequestException("Sorry! You have already cast your vote in this survey");
        }

        //-- Vote Saved, Return the updated Survey Response now --

        // Retrieve Vote Counts of every choice belonging to the current survey
        List<ChoiceVoteCount> votes = voteRepository.countBySurveyIdGroupByChoiceId(surveyId);

        Map<Long, Long> choiceVotesMap = votes.stream()
                .collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

        // Retrieve survey creator details
        User creator = userRepository.findById(survey.getCreatedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", survey.getCreatedBy()));

        return ModelMapper.mapSurveyToSurveyResponse(survey, choiceVotesMap, creator, vote.getChoice().getId());
    }


    private void validatePageNumberAndSize(int page, int size) {
        if(page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if(size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }

    private Map<Long, Long> getChoiceVoteCountMap(List<Long> surveyIds) {
        // Retrieve Vote Counts of every Choice belonging to the given surveyIds
        List<ChoiceVoteCount> votes = voteRepository.countBySurveyIdInGroupByChoiceId(surveyIds);

        Map<Long, Long> choiceVotesMap = votes.stream()
                .collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

        return choiceVotesMap;
    }

    private Map<Long, Long> getSurveyUserVoteMap(UserPrincipal currentUser, List<Long> surveyIds) {
        // Retrieve Votes done by the logged in user to the given surveyIds
        Map<Long, Long> surveyUserVoteMap = null;
        if(currentUser != null) {
            List<Vote> userVotes = voteRepository.findByUserIdAndSurveyIdIn(currentUser.getId(), surveyIds);

            surveyUserVoteMap = userVotes.stream()
                    .collect(Collectors.toMap(vote -> vote.getSurvey().getId(), vote -> vote.getChoice().getId()));
        }
        return surveyUserVoteMap;
    }

    Map<Long, User> getSurveyCreatorMap(List<Survey> surveys) {
        // Get Survey Creator details of the given list of surveys
        List<Long> creatorIds = surveys.stream()
                .map(Survey::getCreatedBy)
                .distinct()
                .collect(Collectors.toList());

        List<User> creators = userRepository.findByIdIn(creatorIds);
        Map<Long, User> creatorMap = creators.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return creatorMap;
    }
}