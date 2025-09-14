package com.dipa.notefournote.users;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Ignoring fields that are not available in DTO class
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "receivedShares", ignore = true)
    UserEntity toEntity(User user);

    User toModel(UserEntity userEntity);

}