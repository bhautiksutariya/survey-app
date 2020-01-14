package com.survey.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UserDetailsDTO {
	
	
    private Long id;
	
    private String username;
	
    private String name;
    
    public UserDetailsDTO() {
		super();
	}

    public UserDetailsDTO(Long id, String username, String name) {
        this.id = id;
        this.username = username;
        this.name = name;
    }
    
}
