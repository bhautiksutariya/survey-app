package com.survey.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UserAvailability {
    private Boolean available;
    
    public UserAvailability() {
		super();
	}

    public UserAvailability(Boolean available) {
        this.available = available;
    }
}
