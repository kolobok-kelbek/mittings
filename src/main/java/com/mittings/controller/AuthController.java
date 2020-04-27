package com.mittings.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.mittings.configuration.SecurityConstants;
import com.mittings.converter.SimpleConverter;
import com.mittings.converter.exception.ConvertException;
import com.mittings.converter.factory.ConverterFactory;
import com.mittings.cookie.AuthCookieUtil;
import com.mittings.entity.User;
import com.mittings.model.input.Sign;
import com.mittings.model.view.UserView;
import com.mittings.service.UserService;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {
  private final UserService userService;

  private final SimpleConverter<Sign, User> signToUserConverter;

  private final SimpleConverter<User, UserView> UserToUserViewConverter;

  @Autowired
  public AuthController(final UserService userService, final ConverterFactory converterFactory) {
    this.userService = userService;

    this.signToUserConverter = converterFactory.createSimpleConverter();
    this.UserToUserViewConverter = converterFactory.createSimpleConverter();
  }

  @PostMapping("/signup")
  public UserView signUp(@Valid @RequestBody final Sign sign) throws ConvertException {
    if (userService.userExists(sign.getUsername())) {
      throw new ResponseStatusException(BAD_REQUEST, "User with this phone number exist.");
    }

    Sign signWithRoles =
        Sign.builder().password(sign.getPassword()).username(sign.getUsername()).build();

    User user = signToUserConverter.convert(signWithRoles, User.class);

    userService.createUser(user);

    return UserToUserViewConverter.convert(user, UserView.class);
  }

  @GetMapping("/logout")
  public void logout(final HttpServletResponse response) {
    AuthCookieUtil.clear(response, SecurityConstants.TOKEN_COOKIE);
  }
}
