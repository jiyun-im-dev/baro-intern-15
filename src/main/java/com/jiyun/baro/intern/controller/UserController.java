package com.jiyun.baro.intern.controller;

import com.jiyun.baro.intern.dto.request.SignupRequest;
import com.jiyun.baro.intern.dto.response.UserResponse;
import com.jiyun.baro.intern.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@RequestBody SignupRequest request) {
        UserResponse response = userService.signup(request);
        return ResponseEntity.ok(response);
    }
}
