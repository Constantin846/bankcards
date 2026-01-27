package tk.project.bankcards.mapper;

import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import tk.project.bankcards.dto.BankCardCreateDto;
import tk.project.bankcards.dto.BankCardInfoDto;
import tk.project.bankcards.dto.BankCardShortInfoDto;
import tk.project.bankcards.entity.BankCardEntity;
import tk.project.bankcards.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface BankCardMapper {

  BankCardMapper MAPPER = Mappers.getMapper(BankCardMapper.class);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "owner", ignore = true)
  @Mapping(target = "status", ignore = true)
  BankCardEntity toBankCardEntity(BankCardCreateDto bankCardCreateDto);

  @Mapping(target = "number", source = "number", qualifiedByName = "mapNumber")
  BankCardInfoDto toBankCardInfoDto(BankCardEntity bankCard);

  @Mapping(target = "number", source = "number", qualifiedByName = "mapNumber")
  @Mapping(target = "ownerId", source = "owner", qualifiedByName = "mapOwnerToId")
  BankCardShortInfoDto toBankCardShortInfoDto(BankCardEntity bankCards);

  @Mapping(target = "number", source = "number", qualifiedByName = "mapNumber")
  @Mapping(target = "ownerId", source = "owner", qualifiedByName = "mapOwnerToId")
  List<BankCardShortInfoDto> toBankCardShortInfoDto(List<BankCardEntity> bankCards);

  @Named("mapNumber")
  default String mapNumber(Long number) {
    return "**** **** **** " + Long.toString(number).substring(12);
  }

  @Named("mapOwnerToId")
  default UUID mapOwnerToId(UserEntity owner) {
    return owner.getId();
  }
}
