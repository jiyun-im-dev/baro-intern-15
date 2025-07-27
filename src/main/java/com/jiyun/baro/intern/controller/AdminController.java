package com.jiyun.baro.intern.controller;

import com.jiyun.baro.intern.dto.response.UserResponse;
import com.jiyun.baro.intern.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PatchMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> grantAdminRole(@PathVariable Long userId) {
        UserResponse response = adminService.grantAdminRole(userId);
        return ResponseEntity.ok(response);
    }
}
