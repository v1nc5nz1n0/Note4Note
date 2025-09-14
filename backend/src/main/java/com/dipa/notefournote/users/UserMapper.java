package com.dipa.notefournote.users;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserEntity toEntity(User user);

    User toModel(UserEntity userEntity);

}