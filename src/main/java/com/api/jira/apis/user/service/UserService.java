package com.api.jira.apis.user.service;

import com.api.jira.apis.user.entity.UserEntity;
import com.api.jira.apis.user.model.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    private final UserDbService userDbService;

    public UserService(UserDbService userDbService) {
        this.userDbService = userDbService;
    }

    public ResponseEntity<?> saveUser(String email, String name, String picture) {
        Optional<UserEntity> existingUserOpt = userDbService.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            // USER EXISTS: Update dynamic fields and return success (200 OK)
            UserEntity existingUser = existingUserOpt.get();
            existingUser.setUsername(name);
            existingUser.setPictureUrl(picture);
            existingUser.setLastLogin(LocalDateTime.now());

            Optional<UserDto> updatedUser = userDbService.save(existingUser);
            return ResponseEntity.ok().body(updatedUser);
        } else {
            // BRAND NEW USER: Create them and return success (200 OK)
            UserEntity newUser = new UserEntity();
            newUser.setEmail(email);
            newUser.setUsername(name);
            newUser.setPictureUrl(picture);
            newUser.setCreatedAt(LocalDateTime.now());

            Optional<UserDto> savedUser = userDbService.save(newUser);
            return ResponseEntity.ok().body(savedUser);
        }
    }

}
