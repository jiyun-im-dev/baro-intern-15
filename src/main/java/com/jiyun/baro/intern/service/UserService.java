package com.jiyun.baro.intern.service;

import com.jiyun.baro.intern.dto.request.SignupRequest;
import com.jiyun.baro.intern.dto.response.UserResponse;
import com.jiyun.baro.intern.entity.User;
import com.jiyun.baro.intern.enums.Role;
import com.jiyun.baro.intern.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public UserResponse signup(SignupRequest request) {
        // 아이디 중복 확인
        userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        });

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 사용자 등록
        User user = User.builder()
                .username(request.getUsername())
                .nickname(request.getNickname())
                .password(encodedPassword)
                .role(Role.USER)
                .build();
        userRepository.save(user);

        return new UserResponse(user);
    }
}
