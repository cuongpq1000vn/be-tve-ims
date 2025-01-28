package vn.codezx.triviet.services.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import vn.codezx.triviet.component.KeyGen;
import vn.codezx.triviet.config.GoogleClientConfig;
import vn.codezx.triviet.constants.MessageCode;
import vn.codezx.triviet.constants.RoleName;
import vn.codezx.triviet.dtos.staff.StaffDTO;
import vn.codezx.triviet.dtos.staff.request.AddTokenToUserDTO;
import vn.codezx.triviet.dtos.staff.request.GetRefreshTokenDTO;
import vn.codezx.triviet.entities.staff.KeyPairEntity;
import vn.codezx.triviet.entities.staff.Staff;
import vn.codezx.triviet.entities.staff.id.KeyPairId;
import vn.codezx.triviet.exceptions.TveException;
import vn.codezx.triviet.mappers.staff.StaffToDTOMapper;
import vn.codezx.triviet.repositories.KeyPairRepository;
import vn.codezx.triviet.repositories.RoleRepository;
import vn.codezx.triviet.repositories.StaffRepository;
import vn.codezx.triviet.services.AuthService;
import vn.codezx.triviet.utils.LogUtil;
import vn.codezx.triviet.utils.MessageUtil;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final GoogleClientConfig googleClientConfig;
  private final MessageUtil messageUtil;
  private final KeyPairRepository keyPairRepositories;
  private final StaffRepository staffRepository;
  private final StaffToDTOMapper staffToDTOMapper;
  private final KeyGen keyGen;
  private final RoleRepository roleRepository;

  AuthServiceImpl(KeyGen keyGen, GoogleClientConfig googleClientConfig, MessageUtil messageUtil,
      KeyPairRepository keyPairRepositories, StaffRepository staffRepository,
      StaffToDTOMapper staffToDTOMapper, RoleRepository roleRepository) {
    this.googleClientConfig = googleClientConfig;
    this.messageUtil = messageUtil;
    this.keyPairRepositories = keyPairRepositories;
    this.staffRepository = staffRepository;
    this.roleRepository = roleRepository;
    this.staffToDTOMapper = staffToDTOMapper;
    this.keyGen = keyGen;
    initToken();
  }

  private void initToken() {
    List<KeyPairEntity> keyPairEntities = keyPairRepositories.findAll();
    if (keyPairEntities.isEmpty()) {
      log.info("No token yet, generate new token");
      generateKeyPair(UUID.randomUUID().toString());
      return;
    }

    log.info("Already has token. skipping generation.");
  }

  @Override
  @Transactional
  public void generateKeyPair(String requestId) {
    log.info(LogUtil.buildFormatLog(requestId, "Delete old keys"));
    List<KeyPairEntity> oldKeys = keyPairRepositories.findAll();

    keyPairRepositories.deleteAll();
    var keys = keyGen.generateKeyPair(requestId);
    var keyId = KeyPairId.builder().publicKey(keyGen.toString(keys.getPublic()))
        .privateKey(keyGen.toString(keys.getPrivate())).build();
    var keyPair = KeyPairEntity.builder().id(keyId).build();

    resetToken(requestId, keyPair, oldKeys);

    keyPairRepositories.save(keyPair);
    log.info(LogUtil.buildFormatLog(requestId, "New keys created"));
  }

  private void resetToken(String requestId, KeyPairEntity newKey, List<KeyPairEntity> oldKeys) {
    if (oldKeys.isEmpty()) {
      return;
    }
    KeyPairEntity old = oldKeys.get(0);
    staffRepository.findAll().forEach(adminEntity -> {
      adminEntity.setRefreshToken(keyGen.rsaEncrypt(requestId, newKey.getId(),
          keyGen.rsaDecrypt(requestId, old.getId(), adminEntity.getRefreshToken())));
      staffRepository.save(adminEntity);
    });
  }

  @Override
  public GoogleIdToken verifyIdToken(String requestId, String token) {
    GoogleIdTokenVerifier verifier =
        new Builder(googleClientConfig.getHttpTransport(), googleClientConfig.getGsonFactory())
            .setAudience(Collections
                .singleton(googleClientConfig.getClientSecrets().getDetails().getClientId()))
            .build();
    try {
      return verifier.verify(token);
    } catch (GeneralSecurityException | IOException e) {
      log.error(LogUtil.buildFormatLog(requestId, "Failed to validate client"),
          e.fillInStackTrace());
      throw new TveException(MessageCode.MESSAGE_UNAUTHORIZED_ERROR.getCode(),
          messageUtil.getMessage(MessageCode.MESSAGE_UNAUTHORIZED_ERROR, requestId));
    }
  }

  @Override
  @Transactional
  public StaffDTO addUser(String requestId, AddTokenToUserDTO addTokenToUserDTO) {
    Staff staff =
        staffRepository.findByEmailAddressAndIsDeleteIsFalse(addTokenToUserDTO.getEmail());
    List<KeyPairEntity> keyPairEntities = keyPairRepositories.findAll();
    if (keyPairEntities.isEmpty()) {
      log.error(LogUtil.buildFormatLog(requestId, "No key pair"), addTokenToUserDTO.getCode());
      throw new TveException(MessageCode.MESSAGE_NOT_FOUND.getCode(),
          messageUtil.getMessage(MessageCode.MESSAGE_NOT_FOUND, requestId));
    }
    KeyPairEntity keyPair = keyPairEntities.getFirst();
    if (ObjectUtils.isEmpty(staff)) {
      Staff entity = Staff.builder().code(addTokenToUserDTO.getCode())
          .emailAddress(addTokenToUserDTO.getEmail()).firstName(addTokenToUserDTO.getFirstName())
          .lastName(addTokenToUserDTO.getLastName()).phoneNumber(addTokenToUserDTO.getPhoneNumber())
          .avatarUrl(addTokenToUserDTO.getAvatarUrl())
          .refreshToken(
              keyGen.rsaEncrypt(requestId, keyPair.getId(), addTokenToUserDTO.getRefreshToken()))
          .build();

      entity.getRoles().add(roleRepository.findByNameAndIsDeleteIsFalse(RoleName.TEACHER));
      return staffToDTOMapper.toDto(staffRepository.save(entity));
    } else {
      if (staff.getCode().isEmpty())
        staff.setCode(addTokenToUserDTO.getCode());
      if (staff.getAvatarUrl().isEmpty())
        staff.setAvatarUrl(addTokenToUserDTO.getAvatarUrl());
      staff.setRefreshToken(
          keyGen.rsaEncrypt(requestId, keyPair.getId(), addTokenToUserDTO.getRefreshToken()));
      return staffToDTOMapper.toDto(staff);
    }

  }

  @Override
  public StaffDTO getRefreshToken(String requestId, GetRefreshTokenDTO getRefreshTokenDTO) {
    Staff staff = staffRepository.findByCode(getRefreshTokenDTO.getUserId());
    if (ObjectUtils.isEmpty(staff)) {
      log.error(LogUtil.buildFormatLog(requestId, "No user with id: {}"),
          getRefreshTokenDTO.getUserId());
      throw new TveException(MessageCode.MESSAGE_UNAUTHORIZED_ERROR.getCode(),
          messageUtil.getMessage(MessageCode.MESSAGE_UNAUTHORIZED_ERROR, requestId));
    }
    List<KeyPairEntity> keyPairEntities = keyPairRepositories.findAll();
    if (keyPairEntities.isEmpty()) {
      log.error(LogUtil.buildFormatLog(requestId, "No key pair"), getRefreshTokenDTO.getUserId());
      throw new TveException(MessageCode.MESSAGE_NOT_FOUND.getCode(),
          messageUtil.getMessage(MessageCode.MESSAGE_NOT_FOUND, requestId));
    }
    var staffDto = staffToDTOMapper.toDto(staff);
    staffDto.setRefreshToken(
        keyGen.rsaDecrypt(requestId, keyPairEntities.get(0).getId(), staff.getRefreshToken()));
    return staffDto;

  }
}
