package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.dto.UserLoginRequest;
import com.moneybuddy.moneylog.repository.UserRepository;
import com.moneybuddy.moneylog.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class UserService {
    private final UserRepository userRepository;

    public void login(UserLoginRequest request){
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(()->new IllegalArgumentException("이메일이 존재하지 않아요."));

        if(!user.getPassword().equals(request.getPassword())){
            throw new IllegalArgumentException("비밀번호가 틀렸어요.");
        }
    }
}
