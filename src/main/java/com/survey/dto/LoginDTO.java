package com.survey.dto;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class LoginDTO {

	@ApiModelProperty(value = "test email",position = 1)
	@NotBlank
    private String usernameOrEmail;

	@ApiModelProperty(value = "test password",position = 2)
    @NotBlank
    private String password;
	
}
