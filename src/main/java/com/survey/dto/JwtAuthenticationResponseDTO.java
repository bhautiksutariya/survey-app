package com.survey.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class JwtAuthenticationResponseDTO {

    private String tokenType = "Bearer";
	
	private String accessToken;
    
    public JwtAuthenticationResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }
	
}
