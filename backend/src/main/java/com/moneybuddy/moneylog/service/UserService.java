package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.dto.UserSignupRequest;
import com.moneybuddy.moneylog.repository.UserRepository;
import com.moneybuddy.moneylog.domain.User;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 💥 추가

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder; // 💥 주입
    }

    public void signup(UserSignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword()); // 💥 암호화
        User user = new User(request.getEmail(), encodedPassword); // 💥 암호화된 비밀번호 저장
        userRepository.save(user);
    }
}
