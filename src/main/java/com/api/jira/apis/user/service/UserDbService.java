package com.api.jira.apis.user.service;

import com.api.jira.apis.user.entity.UserEntity;
import com.api.jira.apis.user.mapper.UserMapper;
import com.api.jira.apis.user.model.UserDto;
import com.api.jira.apis.user.model.UserSuggestionsDto;
import com.api.jira.apis.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserDbService {
    private final UserMapper userMapper;

    private final UserRepository userRepository;

    public UserDbService(UserMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<UserDto> save(UserEntity user) {
        UserEntity savedUser = userRepository.save(user);
        UserDto userDto = userMapper.toDto(savedUser);
        return Optional.of(userDto);
    }

    public Optional<UserDto> findByReporterEmail(String email) {
        Optional<UserEntity> userEntityOptional = userRepository.findByEmail(email);
        return userEntityOptional.map(userMapper::toDto);
    }

    public Optional<UserEntity> findByUserId(Integer userId){
        return userRepository.findByUserId(userId);
    }

    public Optional<UserEntity> findByUserName(String userName){
        return userRepository.findByUserName(userName);
    }

    public Optional<UserEntity> findByEmailId(String email){
        return userRepository.findByEmail(email);
    }

    public List<UserSuggestionsDto> findByProjectId(Integer projectId) {
        return userRepository.findByProjectId(projectId, null)
                .stream()
                .map(userMapper::toUserSuggestionsDto)
                .toList();
    }
}
