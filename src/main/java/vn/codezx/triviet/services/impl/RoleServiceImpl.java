package vn.codezx.triviet.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.codezx.triviet.dtos.staff.RoleDTO;
import vn.codezx.triviet.mappers.staff.RoleToDTOMapper;
import vn.codezx.triviet.repositories.RoleRepository;
import vn.codezx.triviet.services.RoleService;

@Service
public class RoleServiceImpl implements RoleService {
  private final RoleRepository roleRepository;
  private final RoleToDTOMapper roleToDTOMapper;

  public RoleServiceImpl(RoleRepository roleRepository, RoleToDTOMapper roleToDTOMapper) {
    this.roleRepository = roleRepository;
    this.roleToDTOMapper = roleToDTOMapper;
  }

  @Override
  public Page<RoleDTO> getRoles(String requestId, String query, Pageable pageable) {
    return roleRepository.findAllByIsDeleteIsFalse(pageable).map(roleToDTOMapper::toDto);
  }

}
