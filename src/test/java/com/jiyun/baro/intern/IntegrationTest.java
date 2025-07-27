package com.jiyun.baro.intern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiyun.baro.intern.dto.request.SignupRequest;
import com.jiyun.baro.intern.entity.User;
import com.jiyun.baro.intern.enums.Role;
import com.jiyun.baro.intern.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트 후 DB 롤백
public class IntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Nested
	@DisplayName("회원가입 테스트")
	class SignupTest {

		@Test
		@DisplayName("성공")
		void signup_success() throws Exception {
			// given
			SignupRequest request = new SignupRequest("testuser", "testpwd", "test");
			String jsonRequest = objectMapper.writeValueAsString(request);

			// when & then
			mockMvc.perform(post("/signup")
					.contentType("MediaType.APPLICATION_JSON")
					.content(jsonRequest))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("testuser"))
				.andDo(print());
		}

		@Test
		@DisplayName("실패 - 사용자 중복")
		void signup_fail_duplicate_user() throws Exception {
			// given: 이미 사용자가 가입된 상태
			User existingUser = User.builder().username("testuser").password("testpwd").nickname("test").build();
			userRepository.save(existingUser);

			SignupRequest request = new SignupRequest("testuser", "testpwd", "test");
			String jsonRequest = objectMapper.writeValueAsString(request);

			// when & then
			mockMvc.perform(post("/signup")
					.contentType("MediaType.APPLICATION_JSON")
					.content(jsonRequest))
				.andExpect(status().isConflict()) // ErrorCode.USER_ALREADY_EXISTS = 409 Conflict
				.andExpect(jsonPath("$.error.code").value("USER_ALREADY_EXISTS")) // ErrorResponse > error > code
				.andDo(print());
		}
	}

	@Nested
	@DisplayName("로그인 테스트")
	class LoginTest {

		@Test
		@DisplayName("성공")
		void login_success() throws Exception {
			// given: 회원 저장
			User existingUser = User.builder().username("testuser").password("testpwd").nickname("test").build();
			userRepository.save(existingUser);

			// when & then
			mockMvc.perform(post("/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"username\": \"testuser\", \"password\": \"testpwd\"}"))
				.andExpect(status().isOk())
				.andExpect(header().exists("Authorization"))
				.andDo(print());
		}

		@Test
		@DisplayName("실패 - 비밀번호 불일치")
		void login_fail_wrong_credentials() throws Exception {
			// given: 회원 저장
			User existingUser = User.builder().username("testuser").password("testpwd").nickname("test").build();
			userRepository.save(existingUser);

			// when & then
			mockMvc.perform(post("/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"username\":\"testuser\", \"password\":\"wrongpwd\"}"))
				.andExpect(status().isUnauthorized()) // ErrorCode.INVALID_CREDENTIALS = 401 Unauthorized
				.andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"))
				.andDo(print());
		}
	}

	@Nested
	@DisplayName("관리자 권한 부여 테스트")
	class AdminRoleTest {

		private User testUser;

		// 각 테스트 전에 일반 사용자를 미리 생성
		@BeforeEach
		void setup() {
			testUser = User.builder().username("testuser").password("testpwd").nickname("test").role(Role.USER).build();
			userRepository.save(testUser);
		}

		@Test
		@DisplayName("성공 - 관리자가 요청")
		@WithMockUser(roles = "ADMIN") // ADMIN 역할을 가진 가짜 사용자로 로그인한 상태
		void grant_admin_role_by_admin() throws Exception {
			// given: @BeforeEach에서 testUser 생성됨

			// when & then
			mockMvc.perform(patch("/admin/users/{userId}/roles", testUser.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("ADMIN"))
				.andDo(print());
		}

		@Test
		@DisplayName("실패 - 일반 사용자가 요청")
		@WithMockUser(roles = "USER") // USER 역할을 가진 가짜 사용자로 로그인한 상태
		void grant_admin_role_by_user() throws Exception {
			// given: @BeforeEach에서 testUser 생성됨

			// when & then
			mockMvc.perform(patch("/admin/users/{userId}/roles", testUser.getId()))
				.andExpect(status().isForbidden()) // ErrorCode.ACCESS_DENIED = 403 Forbidden
				.andDo(print());
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 사용자에게 권한 부여")
		@WithMockUser(roles = "ADMIN")
		void grant_admin_role_to_non_existent_user() throws Exception {
			// given
			long nonExistentUserId = 999L;

			// when & then
			mockMvc.perform(patch("/admin/users/{userId}/roles", nonExistentUserId))
				.andExpect(status().isNotFound()) // ErrorCode.USER_NOT_FOUND = 404 Not Found
				.andDo(print());
		}
	}
}
