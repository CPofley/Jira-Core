package com.api.jira.apis.user.service;

import com.api.jira.apis.exceptions.ExceptionTypes.UserNotFoundException;
import com.api.jira.apis.user.entity.UserEntity;
import com.api.jira.apis.user.model.UserDto;
import com.api.jira.apis.user.model.UserProfileDto;
import com.api.jira.apis.user.model.UserSuggestionsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
            if(Objects.isNull(existingUser.getPictureUrl()) || existingUser.getPictureUrl().isEmpty()) {
                existingUser.setPictureUrl(picture);
            }
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

    public List<UserSuggestionsDto> getUsersByProject(Integer projectId) {
        return userDbService.findByProjectId(projectId);
    }

    public UserProfileDto getUserProfileLinkByEmailId(String email){
        UserProfileDto userProfileDto =   userDbService.findByReporterEmail(email);
        if(userProfileDto==null){
            throw new UserNotFoundException("User not found with email : "+email);
        }
        return userProfileDto;
    }
}
