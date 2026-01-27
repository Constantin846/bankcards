package tk.project.bankcards.service;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.project.bankcards.dto.UserInfoDto;
import tk.project.bankcards.dto.UserRegisterDto;
import tk.project.bankcards.dto.UserUpdateDto;
import tk.project.bankcards.entity.UserEntity;
import tk.project.bankcards.enums.Role;
import tk.project.bankcards.exception.UserConflictException;
import tk.project.bankcards.exception.UserNotFoundException;
import tk.project.bankcards.mapper.UserMapper;
import tk.project.bankcards.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userRepository
        .findByEmail(email)
        .orElseThrow(
            () ->
                new UsernameNotFoundException(
                    String.format(
                        "Не найдены данные пользователя с почтой %s для аутентификации", email)));
  }

  public UserInfoDto register(UserRegisterDto newUser) {
    throwExceptionIfEmailExists(newUser.email());

    UserEntity userEntity = userMapper.toUserEntity(newUser);
    userEntity.setPassword(passwordEncoder.encode(newUser.password()));
    userEntity.setRole(Role.USER);
    userRepository.save(userEntity);

    log.debug(
        "Пользователь c именем {} и почтой {} сохранен с id: {}.",
        userEntity.getName(),
        userEntity.getEmail(),
        userEntity.getId());
    return userMapper.toUserInfoDto(userEntity);
  }

  @Transactional
  public UserInfoDto update(UserUpdateDto userUpdateDto) {
    UserEntity existingUser = getUserById(userUpdateDto.id());
    UserEntity updatedUser = updateUserFields(existingUser, userUpdateDto);
    userRepository.save(updatedUser);

    log.debug(
        "Пользователь с id {}, c именем {} и почтой {} обновлен.",
        updatedUser.getId(),
        updatedUser.getName(),
        updatedUser.getEmail());
    return userMapper.toUserInfoDto(updatedUser);
  }

  public UserInfoDto getUserByEmail(String email) {
    UserEntity userByEmail =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new UserNotFoundException(
                        String.format("Пользователь с почтой %s не найден.", email)));
    return userMapper.toUserInfoDto(userByEmail);
  }

  private UserEntity getUserById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () ->
                new UserNotFoundException(String.format("Пользователь с id %s не найден", userId)));
  }

  private void throwExceptionIfEmailExists(String userEmail) {
    if (userRepository.findByEmail(userEmail).isPresent()) {
      throw new UserConflictException(
          String.format("Пользователь c почтой %s уже существует.", userEmail));
    }
  }

  private UserEntity updateUserFields(UserEntity existingUser, UserUpdateDto userUpdateDto) {
    if (Objects.nonNull(userUpdateDto.name()) && !userUpdateDto.name().isBlank()) {
      existingUser.setName(userUpdateDto.name());
    }
    if (Objects.nonNull(userUpdateDto.email()) && !userUpdateDto.email().isBlank()) {
      throwExceptionIfEmailExists(userUpdateDto.email());
      existingUser.setEmail(userUpdateDto.email());
    }
    if (Objects.nonNull(userUpdateDto.password()) && !userUpdateDto.password().isBlank()) {
      existingUser.setPassword(passwordEncoder.encode(userUpdateDto.password()));
    }
    return existingUser;
  }
}
