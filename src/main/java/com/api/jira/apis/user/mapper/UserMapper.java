package com.api.jira.apis.user.mapper;

import com.api.jira.apis.comment.mapper.CommentMapper;
import com.api.jira.apis.user.entity.UserEntity;
import com.api.jira.apis.user.model.UserDto;
import com.api.jira.apis.user.model.UserProfileDto;
import com.api.jira.apis.user.model.UserSuggestionsDto;
import com.api.jira.sharedMapper.mapper.CommonMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CommonMapper.class})
public interface UserMapper {

    UserEntity toEntity(UserDto userDto);

    UserDto toDto(UserEntity userEntity);

    UserSuggestionsDto toUserSuggestionsDto(UserEntity userEntity);

    List<UserDto> toUserDtoList(List<UserEntity> userEntities);

    List<UserSuggestionsDto>  toUserSuggestionsDtoList(List<UserEntity> userEntities);

    UserProfileDto toUserProfileDto(UserDto userDto);
}
