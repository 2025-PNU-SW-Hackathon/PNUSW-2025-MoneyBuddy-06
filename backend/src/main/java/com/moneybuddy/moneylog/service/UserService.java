package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.dto.UserLoginRequest;
import com.moneybuddy.moneylog.dto.UserLoginResponse;
import com.moneybuddy.moneylog.jwt.JwtUtil;
import com.moneybuddy.moneylog.repository.UserRepository;
import com.moneybuddy.moneylog.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserLoginResponse login(UserLoginRequest request){
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(()->new RuntimeException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.createToken(user.getId(), user.getEmail());

        return new UserLoginResponse(token, user.getId(), user.getEmail());

    }
}
