package tk.project.bankcards.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import tk.project.bankcards.dto.UserInfoDto;
import tk.project.bankcards.dto.UserRegisterDto;
import tk.project.bankcards.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserMapper MAPPER = Mappers.getMapper(UserMapper.class);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "role", ignore = true)
  UserEntity toUserEntity(UserRegisterDto userRegisterDto);

  UserInfoDto toUserInfoDto(UserEntity user);
}
