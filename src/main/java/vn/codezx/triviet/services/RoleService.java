package vn.codezx.triviet.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.codezx.triviet.dtos.staff.RoleDTO;

public interface RoleService {

  Page<RoleDTO> getRoles(String requestId, String query, Pageable pageable);

}
