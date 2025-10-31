package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.dto.request.UserSignupRequest;
import com.moneybuddy.moneylog.dto.request.UserLoginRequest;
import com.moneybuddy.moneylog.dto.response.UserLoginResponse;
import com.moneybuddy.moneylog.dto.request.UserDeleteRequest;
import com.moneybuddy.moneylog.repository.UserRepository;
import com.moneybuddy.moneylog.repository.UserDailyQuizRepository;
import com.moneybuddy.moneylog.repository.UserExpRepository;
import com.moneybuddy.moneylog.domain.User;
import com.moneybuddy.moneylog.jwt.JwtUtil;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final UserDailyQuizRepository userDailyQuizRepository;
    private final UserExpRepository userExpRepository;

    // 회원가입
    public void signup(UserSignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword()); // 암호화
        User user = new User(request.getEmail(), encodedPassword); // 암호화된 비밀번호 저장
        userRepository.save(user);
    }

    // 로그인
    public UserLoginResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.createToken(user.getId(), user.getEmail());

        return new UserLoginResponse(token, user.getId(), user.getEmail());
    }
  
    // 회원탈퇴
    public void deleteUser(UserDeleteRequest request, HttpServletRequest httpServletRequest) {
        String token = jwtUtil.resolveToken(httpServletRequest);
        if (token == null) {
            throw new IllegalArgumentException("토큰이 존재하지 않습니다.");
        }

        jwtUtil.validateToken(token);
        String email = jwtUtil.getEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Long userId = user.getId();
        userDailyQuizRepository.deleteByUserId(userId);
        userExpRepository.deleteByUserId(userId);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
    }
}
