package africa.za.atech.spring.aio.functions.profile;

import africa.za.atech.spring.aio.functions.admin.dto.ForgotDTO;
import africa.za.atech.spring.aio.functions.admin.dto.UserProfileDTO;
import africa.za.atech.spring.aio.functions.admin.service.UsersService;
import africa.za.atech.spring.aio.utils.Alert;
import africa.za.atech.spring.aio.utils.HelperTools;
import africa.za.atech.spring.aio.utils.OutputTool;
import africa.za.atech.spring.aio.utils.enums.AlertType;
import africa.za.atech.spring.aio.utils.enums.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UsersService usersService;

    @Value("${atech.app.name}")
    private String appName;

    @GetMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        return "redirect:/login?logout=true";
    }

    @GetMapping(value = {"/forgot"})
    public String showForgotForm(Model model,
                                 @RequestParam(name = "complete", required = false) boolean complete) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        model.addAttribute("profileObject", profile);

        if (!complete) {
            model.addAttribute("formObject", new ForgotDTO());
            return "forgot/forgot_form";
        } else {
            model.addAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, "Temp password has been sent to your registered email.")));
            return "forgot/forgot_result";
        }
    }

    @PostMapping("/forgot")
    public String processForgotPassword(Model model, RedirectAttributes redirectAttributes,
                                        @Validated @ModelAttribute(name = "formObject") ForgotDTO form) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        model.addAttribute("profileObject", profile);
        redirectAttributes.addFlashAttribute("profileObject", profile);

        OutputTool outputTool = usersService.forgotPassword(form.getUsername());
        if (outputTool.getResult() == Result.SUCCESS) {
            return "redirect:/forgot?complete=true";
        } else {
            model.addAttribute("formObject", form);
            model.addAttribute("alertList", List.of(new Alert(AlertType.DANGER, outputTool.getComment())));
            return "forgot/forgot_form";
        }
    }

    @GetMapping(value = {"/login"})
    public String showLoginForm(Model model,
                                @RequestParam(name = "error", required = false) String error) {
        if (error != null) {
            model.addAttribute("danger", true);
            model.addAttribute("alertList", List.of(new Alert(AlertType.DANGER, "Login Failed. Please try again later.")));
        }
        return "login/login_form";
    }

    @GetMapping(value = {"/profile"})
    public String getPasswordForm(Model model,
                                  @RequestParam(name = "pwd", required = false) boolean isPassword) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        model.addAttribute("profileObject", profile);
        if (isPassword) {
            return "profile/profile_password_form";
        } else {
            return "profile/profile_update_form";
        }
    }

    @PostMapping(value = {"/profile"})
    public String updateUserDetails(RedirectAttributes redirectAttributes,
                                    @Validated @ModelAttribute(name = "profileObject") UserProfileDTO userProfileDTO) {
        usersService.addMandatoryAttributes(redirectAttributes);
        OutputTool outputTool = usersService.updateProfile(userProfileDTO, null);
        redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, outputTool.getComment())));
        return "redirect:/home?updated=true";

    }

    @GetMapping(value = {"/", "/home"})
    public String showUserHomePage(Model model) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(model, profile);
        String welcome = HelperTools.getString("static/html/home-intro.html")
                .replaceAll("~NAME~", profile.getName())
                .replaceAll("~APP_NAME~", appName);
        model.addAttribute("welcome", welcome);
        return "home";
    }

}
