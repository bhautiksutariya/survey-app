package com.survey.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ApiResponseDTO {

	private Boolean success;
    private String message;
    
    public ApiResponseDTO(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
	
}
