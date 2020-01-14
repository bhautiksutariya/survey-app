package com.survey.dto;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PageableResponse<T> {

    private List<T> content;
	
    private int page;
	
    private int size;
	
    private long totalElements;
	
    private int totalPages;
	
    private boolean isLast;

    public PageableResponse() {
    	super();
    }

    public PageableResponse(List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.isLast = last;
    }
    
}
