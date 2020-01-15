package com.survey.controller;

import com.survey.dto.*;
import com.survey.model.*;
import com.survey.repository.*;
import com.survey.security.CurrentUser;
import com.survey.security.UserPrincipal;
import com.survey.service.SurveyService;
import com.survey.util.AppConstants;

import com.survey.exception.ResourceNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api")
@Api(value = "User Resource", description = "To get user detail and surveys belong that particular user", tags = {"User Resource"})
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private SurveyService surveyService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @ApiOperation(value = "User Profile", tags = {"User Resource"})
    @GetMapping("/users/{username}")
    public UserProfile getUserProfile(@PathVariable(value = "username") String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        long surveyCount = surveyRepository.countByCreatedBy(user.getId());
        long voteCount = voteRepository.countByUserId(user.getId());

        UserProfile userProfile = new UserProfile(user.getId(), user.getUsername(), user.getName(), user.getCreatedAt(), surveyCount, voteCount);

        return userProfile;
    }

    @ApiOperation(value = "Get Surveys Created By User", tags = {"User Resource"})
    @GetMapping("/users/{username}/surveys")
    public PageableResponse<SurveyResponseDTO> getSurveysCreatedBy(@PathVariable(value = "username") String username,
                                                         @CurrentUser UserPrincipal currentUser,
                                                         @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                         @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return surveyService.getSurveysCreatedBy(username, currentUser, page, size);
    }


    @ApiOperation(value = "Get Surveys Voted By User", tags = {"User Resource"})
    @GetMapping("/users/{username}/votes")
    public PageableResponse<SurveyResponseDTO> getSurveysVotedBy(@PathVariable(value = "username") String username,
                                                       @CurrentUser UserPrincipal currentUser,
                                                       @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                       @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return surveyService.getSurveysVotedBy(username, currentUser, page, size);
    }

}
