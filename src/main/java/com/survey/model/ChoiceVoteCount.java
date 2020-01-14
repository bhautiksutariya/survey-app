package com.survey.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ChoiceVoteCount {
    private Long choiceId;
    private Long voteCount;
    

	public ChoiceVoteCount() {
		super();
	}
    
    public ChoiceVoteCount(Long choiceId, Long voteCount) {
        this.choiceId = choiceId;
        this.voteCount = voteCount;
    }



    
}
