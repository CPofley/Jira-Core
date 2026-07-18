package com.api.jira.apis.user.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserProfileDto {
    private String username;
    private String email;
    private String pictureUrl;
}
