package com.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;


public class ResponseDto {
	@Schema(name="AuthDto.LoginRes")
	public record login(
		String accessToken,
		String refreshToken
	) {}
}
