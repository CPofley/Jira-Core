package com.api.jira.apis.project.service;

import com.api.jira.apis.project.entity.ProjectEntity;
import com.api.jira.apis.project.model.AddUserToProjectRequest;
import com.api.jira.apis.project.model.CreateProjectRequest;
import com.api.jira.apis.project.model.ProjectDto;
import com.api.jira.apis.project.repository.ProjectRepository; // 🔴 Now actively used!
import com.api.jira.apis.user.entity.UserEntity;
import com.api.jira.apis.user.model.UserDto;
import com.api.jira.apis.user.service.UserDbService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private final ProjectDbService projectDbService;
    private final UserDbService userDbService;
    private final ProjectRepository projectRepository; // 🔴 Injected so we can run our custom query!

    public ProjectService(ProjectDbService projectDbService, UserDbService userDbService, ProjectRepository projectRepository) {
        this.projectDbService = projectDbService;
        this.userDbService = userDbService;
        this.projectRepository = projectRepository;
    }

    // 🔴 UPDATED: Now grabs creatorEmail to link the user instantly upon creation
    public ResponseEntity<ProjectDto> saveProject(CreateProjectRequest saveProjectRequest, String creatorEmail){
        ProjectDto projectDto = ProjectDto.builder()
                .projectName(saveProjectRequest.getProjectName())
                .projectDescription(saveProjectRequest.getProjectDescription())
                .build();

        ProjectDto savedProject = projectDbService.saveProject(projectDto);

        // Fetch the user & project to build the bridge
        Optional<UserEntity> user = userDbService.findByEmail(creatorEmail);
        Optional<ProjectEntity> project = projectDbService.findProjectById(savedProject.getProjectId());

        if(user.isPresent() && project.isPresent()){
            ProjectEntity projEntity = project.get();
            projEntity.getUsers().add(user.get()); // Link the Google user!
            projectDbService.saveProject(projEntity);
        }

        return ResponseEntity.ok().body(savedProject);
    }

    // 🔴 NEW: Fetches only the projects where this user is explicitly tagged
    public ResponseEntity<List<ProjectDto>> getMyProjects(String email) {
        List<ProjectEntity> myProjects = projectRepository.findByUsers_Email(email);

        // Map the raw entities back into clean DTOs for React
        List<ProjectDto> dtoList = myProjects.stream().map(p ->
                ProjectDto.builder()
                        .projectId(p.getProjectId())
                        .projectName(p.getProjectName())
                        .projectDescription(p.getProjectDescription())
                        .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    public ResponseEntity<ProjectDto> addUserToProject(AddUserToProjectRequest addUserToProjectRequest){
        Optional<ProjectEntity> existingProject = projectDbService.findProjectById(addUserToProjectRequest.getProjectId());
        Optional<UserEntity> existingUser = userDbService.findByUserId(addUserToProjectRequest.getUserId());

        if(existingProject.isEmpty() || existingUser.isEmpty()){
            throw new RuntimeException("There's no Project / User available to tag");
        }
        ProjectEntity project = existingProject.get();
        project.getUsers().add(existingUser.get());
        return ResponseEntity.ok().body(projectDbService.saveProject(project));
    }

    public ResponseEntity<ProjectDto> getProjectForUser(Integer projectId){
        ProjectDto dto =  projectDbService.getProjectById(projectId);
        if(!Objects.isNull(dto)){
            return ResponseEntity.ok().body(dto);
        }
        else{
            throw new RuntimeException("No project found with project ID "+projectId);
        }
    }

    public List<UserDto> getUsersTaggedToProject(Integer projectId){
        return projectDbService.getUsersTaggedToProject(projectId);
    }
}