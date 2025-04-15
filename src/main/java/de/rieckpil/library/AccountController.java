package de.rieckpil.library;

import de.rieckpil.library.model.LibraryUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
@PreAuthorize("isAuthenticated()")
public class AccountController {

  private final UserService userService;
  private final BookLoanService loanService;

  public AccountController(UserService userService, BookLoanService loanService) {
    this.userService = userService;
    this.loanService = loanService;
  }

  @GetMapping("/profile")
  public String viewProfile(Model model) {
    LibraryUser user = userService.getCurrentUser();

    if (user == null) {
      return "redirect:/";
    }

    model.addAttribute("user", user);
    model.addAttribute("profileForm", new ProfileForm(user));
    model.addAttribute("activeLoansCount", loanService.getActiveLoansForUser(user).size());

    return "account/profile";
  }

  @PostMapping("/profile")
  public String updateProfile(
      @Valid @ModelAttribute("profileForm") ProfileForm profileForm,
      BindingResult result,
      Model model,
      RedirectAttributes redirectAttributes) {

    LibraryUser user = userService.getCurrentUser();

    if (user == null) {
      return "redirect:/";
    }

    // If validation fails, re-render the profile page with errors
    if (result.hasErrors()) {
      model.addAttribute("user", user);
      model.addAttribute("activeLoansCount", loanService.getActiveLoansForUser(user).size());
      return "account/profile";
    }

    // Update user with form data
    user.setFirstName(profileForm.getFirstName());
    user.setLastName(profileForm.getLastName());
    user.setPhone(profileForm.getPhone());
    user.setAddress(profileForm.getAddress());

    // Email might be read-only if coming from Keycloak
    // Only update it if the system allows
    if (!user.getEmail().equals(profileForm.getEmail())) {
      user.setEmail(profileForm.getEmail());
    }

    // Save the updated user
    userService.updateUser(user);

    redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
    return "redirect:/account/profile";
  }

  // Form backing class for profile information
  public static class ProfileForm {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    private String phone;

    private String address;

    // Default constructor needed for form binding
    public ProfileForm() {}

    // Constructor to initialize from a LibraryUser
    public ProfileForm(LibraryUser user) {
      this.firstName = user.getFirstName();
      this.lastName = user.getLastName();
      this.email = user.getEmail();
      this.phone = user.getPhone();
      this.address = user.getAddress();
    }

    // Getters and setters
    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getPhone() {
      return phone;
    }

    public void setPhone(String phone) {
      this.phone = phone;
    }

    public String getAddress() {
      return address;
    }

    public void setAddress(String address) {
      this.address = address;
    }
  }
}
