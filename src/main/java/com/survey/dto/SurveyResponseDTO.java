package com.survey.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
public class SurveyResponseDTO {
	
    private Long id;
	
    private String question;
	
    private List<ChoiceResponseDTO> choices;
	
    private UserDetailsDTO createdBy;
	
    private Instant creationDateTime;
	
    private Instant expirationDateTime;
	
    private Boolean isExpired;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long selectedChoice;
    
    private Long totalVotes;

	public SurveyResponseDTO() {
		super();
	}

    
}
