package com.moveit.user.service;

import com.moveit.user.dto.User;
import com.moveit.user.mapper.UserMapper;
import com.moveit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Page<User> getAllUsers(Pageable pageable) {
        return this.userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    public User getUserById(Integer id) {
        return this.userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(User user) {
        if (userRepository.findById(user.getId()).isPresent()) {
            return userMapper.toDto(userRepository.save(userMapper.toEntity(user)));
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public User createSpectator(User user) {
        if (userRepository.findById(user.getId()).isPresent()) {
            throw new RuntimeException("User already exists");
        } else {
            return userMapper.toDto(userRepository.save(userMapper.toEntity(user)));
        }
    }
}