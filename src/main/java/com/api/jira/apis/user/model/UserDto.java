package com.api.jira.apis.user.model;

import lombok.Data;

@Data
public class UserDto {
    private Integer id;
    private String name;
    private String email;
    private String password;
}
