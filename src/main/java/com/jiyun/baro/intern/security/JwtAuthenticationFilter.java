package com.jiyun.baro.intern.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiyun.baro.intern.dto.request.LoginRequest;
import com.jiyun.baro.intern.dto.response.ErrorResponse;
import com.jiyun.baro.intern.enums.Role;
import com.jiyun.baro.intern.exception.ErrorCode;
import com.jiyun.baro.intern.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/login"); // 로그인 요청을 처리할 URL 설정
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // 요청 본문(JSON)을 LoginRequest 타입으로 변환
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            // 인증 토큰 생성
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword(),
                            null // 권한은 인증 성공 후 설정
                    )
            );
        } catch (IOException e) {
            log.error("로그인 정보 파싱 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.info("로그인 성공 및 JWT 생성");

        // 인증 결과에서 사용자 정보 조회
        UserDetailsImpl userDetails = (UserDetailsImpl) authResult.getDetails();
        String username = userDetails.getUsername();
        Role role = userDetails.getUser().getRole();

        // JWT 생성
        String token = jwtUtil.createToken(username, role);

        // 응답 헤더에 JWT 추가
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, token);

        // 응답 상태 코드 및 본문 설정
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString("로그인 성공"));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.info("로그인 실패");

        // 응답 본문 구성
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_CREDENTIALS);

        // 응답 상태 코드 및 본문 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
