package com.example.pcswebserver.web.dao;

import com.example.pcswebserver.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PublicUserMapper {
    PublicUserMapper INSTANCE = Mappers.getMapper(PublicUserMapper.class);

    @Mapping(target = "createdAt", dateFormat = "dd-MM-yyyy HH:mm:ss")
    User fromDto(PublicUser dto);

    @Mapping(target = "createdAt", dateFormat = "dd-MM-yyyy HH:mm:ss")
    PublicUser toDto(User user);
}
