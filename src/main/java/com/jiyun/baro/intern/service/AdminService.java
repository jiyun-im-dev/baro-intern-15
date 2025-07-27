package com.jiyun.baro.intern.service;

import com.jiyun.baro.intern.dto.response.UserResponse;
import com.jiyun.baro.intern.entity.User;
import com.jiyun.baro.intern.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse grantAdminRole(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("해당 ID의 사용자를 찾을 수 없습니다."));

        user.grantAdminRole();

        return UserResponse.of(user);
    }
}
