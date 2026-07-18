package com.api.jira.apis.user.controller;

import com.api.jira.apis.user.model.UserDto;
import com.api.jira.apis.user.model.UserSuggestionsDto;
import com.api.jira.apis.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncUsers(JwtAuthenticationToken auth) {
        Jwt jwt = auth.getToken();
        // get user's data from google
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        String picture = jwt.getClaimAsString("picture");
        return userService.saveUser(email, name, picture);
    }

    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<?> getUsersByProject(@PathVariable Integer projectId) {
        List<UserSuggestionsDto> projectUsers = userService.getUsersByProject(projectId);
        if(!projectUsers.isEmpty()){
            return ResponseEntity.ok(projectUsers);
        }
        else
            return ResponseEntity.badRequest().body("No users found for projectId: " + projectId);

    }


}