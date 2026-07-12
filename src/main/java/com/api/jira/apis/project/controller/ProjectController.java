package com.api.jira.apis.project.controller;

import com.api.jira.apis.project.model.AddUserToProjectRequest;
import com.api.jira.apis.project.model.CreateProjectRequest;
import com.api.jira.apis.project.model.ProjectDto;
import com.api.jira.apis.project.service.ProjectService;
import com.api.jira.apis.user.model.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createProject(@Valid @RequestBody CreateProjectRequest createProjectRequest,
                                           JwtAuthenticationToken auth){
        String createdBy = auth.getToken().getClaimAsString("email");
        return projectService.saveProject(createProjectRequest,createdBy);
    }

    @PostMapping("/add-user")
    public ResponseEntity<ProjectDto> addUserToProject(@Valid @RequestBody AddUserToProjectRequest addUserToProjectRequest){
        return projectService.addUserToProject(addUserToProjectRequest);
    }

    @GetMapping("/get/{projectId}/user")
    public ResponseEntity<ProjectDto> getProjectForUser(@PathVariable Integer projectId){
        return projectService.getProjectForUser(projectId);
    }

    @GetMapping("/{projectId}/users")
    public ResponseEntity<?> getUsersTaggedToProject(@PathVariable Integer projectId){
        List<UserDto> userDtoList = projectService.getUsersTaggedToProject(projectId);
        if(!userDtoList.isEmpty()){
            return ResponseEntity.ok().body(userDtoList);
        }
        else{
            return ResponseEntity.badRequest().body("No active users found for this project");
        }
    }

    @GetMapping("/my-projects")
    public ResponseEntity<List<ProjectDto>> getMyProjects(JwtAuthenticationToken auth) {

        String userEmail = auth.getToken().getClaimAsString("email");
        return projectService.getMyProjects(userEmail);
    }

}
