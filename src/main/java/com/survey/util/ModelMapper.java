package com.survey.util;

import com.survey.dto.*;
import com.survey.model.Survey;
import com.survey.model.User;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelMapper {

    public static SurveyResponseDTO mapSurveyToSurveyResponse(Survey survey, Map<Long, Long> choiceVotesMap, User creator, Long userVote) {
        SurveyResponseDTO surveyResponse = new SurveyResponseDTO();
        surveyResponse.setId(survey.getId());
        surveyResponse.setQuestion(survey.getQuestion());
        surveyResponse.setCreationDateTime(survey.getCreatedAt());
        surveyResponse.setExpirationDateTime(survey.getExpirationDateTime());
        Instant now = Instant.now();
        surveyResponse.setIsExpired(survey.getExpirationDateTime().isBefore(now));

        List<ChoiceResponseDTO> choiceResponses = survey.getChoices().stream().map(choice -> {
            ChoiceResponseDTO choiceResponse = new ChoiceResponseDTO();
            choiceResponse.setId(choice.getId());
            choiceResponse.setText(choice.getText());

            if(choiceVotesMap.containsKey(choice.getId())) {
                choiceResponse.setVoteCount(choiceVotesMap.get(choice.getId()));
            } else {
                choiceResponse.setVoteCount(0);
            }
            return choiceResponse;
        }).collect(Collectors.toList());

        surveyResponse.setChoices(choiceResponses);
        UserDetailsDTO creatorSummary = new UserDetailsDTO(creator.getId(), creator.getUsername(), creator.getName());
        surveyResponse.setCreatedBy(creatorSummary);

        if(userVote != null) {
            surveyResponse.setSelectedChoice(userVote);
        }

        long totalVotes = surveyResponse.getChoices().stream().mapToLong(ChoiceResponseDTO::getVoteCount).sum();
        surveyResponse.setTotalVotes(totalVotes);

        return surveyResponse;
    }
}
