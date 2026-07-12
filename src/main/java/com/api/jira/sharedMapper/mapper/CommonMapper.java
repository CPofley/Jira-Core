package com.api.jira.sharedMapper.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommonMapper {

    // MapStruct will automatically use this method whenever it needs to turn a Date into a LocalDateTime
    default LocalDateTime mapDateToLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }


}
