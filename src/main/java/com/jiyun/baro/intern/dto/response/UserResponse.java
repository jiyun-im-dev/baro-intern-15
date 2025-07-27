package com.jiyun.baro.intern.dto.response;

import com.jiyun.baro.intern.entity.User;
import com.jiyun.baro.intern.enums.Role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserResponse {

	private final String username;
	private final String nickname;
	private final Role role;

	public static UserResponse of(User user) {
		return new UserResponse(
			user.getUsername(),
			user.getNickname(),
			user.getRole()
		);
	}
}
