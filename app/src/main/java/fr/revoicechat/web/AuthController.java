package fr.revoicechat.web;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.revoicechat.model.User;
import fr.revoicechat.representation.user.SignupRepresentation;
import fr.revoicechat.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final UserService userService;

  public AuthController(AuthenticationManager authenticationManager, final UserService userService) {
    this.authenticationManager = authenticationManager;
    this.userService = userService;
  }

  @PutMapping("/signup")
  public User signup(@RequestBody SignupRepresentation user) {
    return userService.create(user);
  }

  @PostMapping(value = "/login", consumes = { APPLICATION_JSON_VALUE })
  public String login(@RequestBody UserPassword user, HttpServletRequest request) {
    Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.username(), user.password()));
    // Save authentication into the SecurityContext
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
    // Bind context to session (so future requests are logged in)
    HttpSession session = request.getSession(true);
    session.setAttribute("SPRING_SECURITY_CONTEXT", context);
    return "User " + user.username() + " logged in";
  }

  public record UserPassword(String username, String password) {}
}
