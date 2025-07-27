package com.jiyun.baro.intern.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiyun.baro.intern.dto.response.ErrorResponse;
import com.jiyun.baro.intern.exception.ErrorCode;
import com.jiyun.baro.intern.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 요청 헤더에서 JWT 추출
        String tokenValue = jwtUtil.getTokenFromRequest(request);

        if (StringUtils.hasText(tokenValue)) {
            if (!jwtUtil.validateToken(tokenValue)) {
                // 토큰이 유효하지 않은 경우 에러 응답 전송
                sendErrorResponse(response, ErrorCode.INVALID_TOKEN);
                return;
            }

            // 토큰에서 사용자 정보 추출
            Claims info = jwtUtil.getUserInfoFromToken(tokenValue);

            try {
                // 사용자 정보를 기반으로 인증(Authentication) 객체 생성
                setAuthenticationInfo(info.getSubject());
            } catch (Exception e) {
                log.error("인증 처리 실패: {}", e.getMessage());
                sendErrorResponse(response, ErrorCode.INVALID_TOKEN);
                return;
            }
        }
    }

    // 인증 정보를 SecurityContext에 설정하는 유틸리티 메서드
    private void setAuthenticationInfo(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // 사용자 정보를 DB에서 조회하여 UserDetails 객체 획득
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 인증 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // SecurityContextHolder에 인증 정보 저장
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    // 에러 응답을 보내는 유틸리티 메서드
    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of(errorCode)));
    }
}
