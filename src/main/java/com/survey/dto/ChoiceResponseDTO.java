package com.survey.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ChoiceResponseDTO {
	
	
    private long id;
	
    private String text;
	
    private long voteCount;

	public ChoiceResponseDTO() {
		super();
	}

}
