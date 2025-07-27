package com.jiyun.baro.intern.dto.response;

import com.jiyun.baro.intern.entity.User;
import com.jiyun.baro.intern.enums.Role;
import lombok.Getter;

@Getter
public class UserResponse {

    private final String username;
    private final String nickname;
    private final Role role;

    public UserResponse(User user) {
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.role = user.getRole();
    }
}
