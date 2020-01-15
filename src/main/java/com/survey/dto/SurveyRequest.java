package com.survey.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class SurveyRequest {
    
	@ApiModelProperty(value = "Survey Question",position = 1)
	@NotBlank
    @Size(max = 140)
    private String question;

	@ApiModelProperty(value = "Survey Options",position = 2)
    @NotNull
    @Size(min = 2,max = 6,message = "Choices must be betweeb 2 and 6")
    @Valid
    private List<ChoiceRequestDTO> choices;

	@ApiModelProperty(value = "Survey Question",position = 3)
    @NotNull
    @Valid
    private SurveyExpiration surveyExpiry;

	public SurveyRequest() {
		super();
	}
    
    
}
