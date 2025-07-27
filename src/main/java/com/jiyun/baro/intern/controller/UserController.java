package com.jiyun.baro.intern.controller;

import com.jiyun.baro.intern.dto.request.SignupRequest;
import com.jiyun.baro.intern.dto.response.UserResponse;
import com.jiyun.baro.intern.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@RequestBody SignupRequest request) {
        log.debug("회원가입 메서드 호출");
        UserResponse response = userService.signup(request);
        log.debug("회원가입 메서드 호출 완료");
        return ResponseEntity.ok(response);
    }
}
