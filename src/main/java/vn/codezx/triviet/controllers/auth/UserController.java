package vn.codezx.triviet.controllers.auth;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.codezx.triviet.dtos.staff.StaffDTO;
import vn.codezx.triviet.dtos.staff.request.GetRefreshTokenDTO;
import vn.codezx.triviet.services.AuthService;

@RestController
@RequestMapping("api/public/user")
public class UserController {

  final AuthService authenticationService;

  UserController(AuthService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @GetMapping("refresh-token/{request-id}")
  public ResponseEntity<StaffDTO> getRefreshToken(@PathVariable("request-id") String requestId,
      @ParameterObject GetRefreshTokenDTO getRefreshTokenDTO) {
    return ResponseEntity
        .ok(authenticationService.getRefreshToken(requestId, getRefreshTokenDTO));
  }
}
