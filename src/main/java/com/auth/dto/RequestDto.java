package com.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RequestDto {
	@Schema(name="AuthDto.LoginReq")
	public record login(
		@Email @NotBlank String email,
		@NotBlank String password
	) {}
}

