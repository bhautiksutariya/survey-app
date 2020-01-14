package com.survey.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SurveyExpiration {

    @NotNull
    private Integer hours;

	public SurveyExpiration() {
		super();
	}

    
}
