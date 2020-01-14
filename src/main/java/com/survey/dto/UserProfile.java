package com.survey.dto;

import java.time.Instant;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UserProfile {
	
    private Long id;
	
    private String username;
	
    private String name;
	
    private Instant joinedAt;
	
    private Long surveyCount;
	
    private Long voteCount;
    
    public UserProfile() {
		super();
	}

    public UserProfile(Long id, String username, String name, Instant joinedAt, Long surveyCount, Long voteCount) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.joinedAt = joinedAt;
        this.surveyCount = surveyCount;
        this.voteCount = voteCount;
    }

}
