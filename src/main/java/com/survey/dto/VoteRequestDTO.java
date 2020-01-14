package com.survey.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class VoteRequestDTO {

	@NotNull
    private Long choiceId;

	public VoteRequestDTO() {
		super();
	}
	
}
