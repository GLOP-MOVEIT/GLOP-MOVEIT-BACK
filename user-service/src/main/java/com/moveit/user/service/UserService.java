package com.moveit.user.service;

import com.moveit.user.dto.User;
import com.moveit.user.dto.UserRequest;
import com.moveit.user.entity.UserEntity;
import com.moveit.user.exception.UserNotFoundException;
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
                .map(this.userMapper::toDto);
    }

    public User getUserById(Integer id) {
        return this.userRepository.findById(id)
                .map(this.userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User updateUser(UserRequest user) {
        if (this.userRepository.findById(user.getUserId()).isPresent()) {
            return this.userMapper.toDto(this.userRepository.save(this.userMapper.toEntity(user)));
        } else {
            throw new UserNotFoundException(user.getUserId());
        }
    }

    public User createSpectator(UserRequest user) {
        return this.userMapper.toDto(this.userRepository.save(this.userMapper.toEntity(user)));
    }

    public UserEntity getUserEntityById(Integer id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public void saveUserEntity(UserEntity user) {
        this.userRepository.save(user);
    }
}