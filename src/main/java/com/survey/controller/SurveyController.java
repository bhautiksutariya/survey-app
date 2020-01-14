package com.survey.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.survey.dto.*;
import com.survey.model.*;
import com.survey.repository.*;
import com.survey.security.CurrentUser;
import com.survey.security.UserPrincipal;
import com.survey.service.SurveyService;
import com.survey.util.AppConstants;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.net.URI;

@RestController
@RequestMapping("/api/surveys")
@Api(value = "Survey Resource", description = "Perform all survey realted query", tags = {"Survey Resource"})
public class SurveyController {

    
    private SurveyRepository surveyRepository;

    private VoteRepository voteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SurveyService surveyService;

    private static final Logger logger = LoggerFactory.getLogger(SurveyController.class);
    
    @Autowired // constructor base injection
    public SurveyController(SurveyRepository surveyRepository,VoteRepository voteRepository) {
		this.surveyRepository=surveyRepository;
		this.voteRepository=voteRepository;
	}

    @ApiOperation(value = "Get Surveys", tags = {"Survey Resource"})
    @GetMapping
    public PageableResponse<SurveyResponseDTO> getSurveys(@CurrentUser UserPrincipal currentUser,
                                                @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return surveyService.getAllSurveys(currentUser, page, size);
    }
    
    @ApiOperation(value = "Create Survey", tags = {"Survey Resource"})
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createSurvey(@Valid @RequestBody SurveyRequest surveyRequest) {
        Survey survey = surveyService.createSurvey(surveyRequest);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{surveyId}")
                .buildAndExpand(survey.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponseDTO(true, "Survey Created Successfully"));
    }
    
    @ApiOperation(value = "Update Survey", tags = {"Survey Resource"})
    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateSurvey(@Valid @RequestBody SurveyUpdateRequest surveyRequest) {
        Survey survey = surveyService.updateSurvey(surveyRequest);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{surveyId}")
                .buildAndExpand(survey.getId()).toUri();

        return ResponseEntity.ok()
                .body(new ApiResponseDTO(true, "Survey Updated Successfully"));
    }
    
    @ApiOperation(value = "Delete Survey", tags = {"Survey Resource"})
    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteSurvey(@NotNull @RequestBody Long surveyId) {
    	
        surveyService.deleteSurvey(surveyId);

        return ResponseEntity.ok()
                .body(new ApiResponseDTO(true, "Survey Deleted Successfully"));
    }

    @ApiOperation(value = "Get Survey", tags = {"Survey Resource"})
    @GetMapping("/{surveyId}")
    public SurveyResponseDTO getSurveyById(@CurrentUser UserPrincipal currentUser,
                                    @PathVariable Long surveyId) {
        return surveyService.getSurveyById(surveyId, currentUser);
    }

    @ApiOperation(value = "Vote Survey", tags = {"Survey Resource"})
    @PostMapping("/votes")
    @PreAuthorize("hasRole('USER')")
    public SurveyResponseDTO castVote(@CurrentUser UserPrincipal currentUser,
                         @NotBlank @RequestParam Long surveyId,
                         @NotBlank @RequestParam Long choiceId) {
        return surveyService.castVoteAndGetUpdatedSurvey(surveyId, choiceId, currentUser);
    }
}
