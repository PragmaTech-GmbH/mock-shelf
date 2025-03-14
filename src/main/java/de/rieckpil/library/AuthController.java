package de.rieckpil.library;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

  @GetMapping("/login")
  public String login(@RequestParam(required = false) String error, Model model) {
    if (error != null) {
      model.addAttribute("error", "Authentication failed");
    }
    return "auth/login";
  }
}
