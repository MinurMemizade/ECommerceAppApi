package com.company.ecommercebackend.api.controller.auth;

import com.company.ecommercebackend.api.model.LoginBody;
import com.company.ecommercebackend.api.model.LoginResponse;
import com.company.ecommercebackend.api.model.PasswordResetBody;
import com.company.ecommercebackend.api.model.RegistrationBody;
import com.company.ecommercebackend.exception.EmailFailureException;
import com.company.ecommercebackend.exception.EmailNotFoundException;
import com.company.ecommercebackend.exception.UserAlreadyExistsException;
import com.company.ecommercebackend.exception.UserNotVerifiedException;
import com.company.ecommercebackend.model.LocalUser;
import com.company.ecommercebackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {


  private UserService userService;


  public AuthenticationController(UserService userService) {
    this.userService = userService;
  }


  @PostMapping("/register")
  public ResponseEntity registerUser(@Valid @RequestBody RegistrationBody registrationBody) {
    try {
      userService.registerUser(registrationBody);
      return ResponseEntity.ok().build();
    } catch (UserAlreadyExistsException ex) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (EmailFailureException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }


  @PostMapping("/login")
  public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginBody loginBody) {
    String jwt = null;
    try {
      jwt = userService.loginUser(loginBody);
    } catch (UserNotVerifiedException ex) {
      LoginResponse response = new LoginResponse();
      response.setSuccess(false);
      String reason = "USER_NOT_VERIFIED";
      if (ex.isNewEmailSent()) {
        reason += "_EMAIL_RESENT";
      }
      response.setFailureReason(reason);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    } catch (EmailFailureException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } else {
      LoginResponse response = new LoginResponse();
      response.setJwt(jwt);
      response.setSuccess(true);
      return ResponseEntity.ok(response);
    }
  }


  @PostMapping("/verify")
  public ResponseEntity verifyEmail(@RequestParam String token) {
    if (userService.verifyUser(token)) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  @PostMapping("/forgot")
  public ResponseEntity forgotPassword(@RequestParam String email)
  {
    try {
      userService.forgotPassword(email);
      return ResponseEntity.ok().build();
    } catch (EmailNotFoundException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } catch (EmailFailureException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("/reset")
  public ResponseEntity resetPassword(@Valid @RequestBody PasswordResetBody body) {
    userService.resetPassword(body);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/me")
  public LocalUser getLoggedInUserProfile(@AuthenticationPrincipal LocalUser user) {
    return user;
  }

}
