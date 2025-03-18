package africa.za.atech.spring.aio.functions.admin.controllers;

import africa.za.atech.spring.aio.functions.admin.SecurityRole;
import africa.za.atech.spring.aio.functions.admin.dto.*;
import africa.za.atech.spring.aio.functions.admin.service.ModuleService;
import africa.za.atech.spring.aio.functions.admin.service.OrganisationService;
import africa.za.atech.spring.aio.functions.admin.service.UsersService;
import africa.za.atech.spring.aio.functions.assistant.AssistantService;
import africa.za.atech.spring.aio.functions.assistant.dto.AssistantDTO;
import africa.za.atech.spring.aio.utils.Alert;
import africa.za.atech.spring.aio.utils.HelperTools;
import africa.za.atech.spring.aio.utils.OutputTool;
import africa.za.atech.spring.aio.utils.enums.AlertType;
import africa.za.atech.spring.aio.utils.enums.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static africa.za.atech.spring.aio.config.Initialize.SYSTEM_ORG_UID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UsersController {

    private final OrganisationService organisationService;
    private final ModuleService moduleService;
    private final UsersService usersService;
    private final AssistantService assistantService;

    @GetMapping(value = {"/admin/users"})
    public String showUserHome(Model model) {
        UserProfileDTO profileDTO = usersService.getProfile(HelperTools.getLoggedInUsername());
        model = usersService.addMandatoryAttributes(model, profileDTO);

        List<UserProfileDTO> recordsList = new ArrayList<>(usersService.getProfiles(profileDTO));
        recordsList.removeIf(u ->
                u.getUsername().equalsIgnoreCase(HelperTools.getLoggedInUsername()) ||
                        (profileDTO.getRoles().equals(SecurityRole.MANAGER) && !u.getModuleUid().equals(profileDTO.getModuleUid()))
        );

        model.addAttribute("recordList", recordsList);
        return "admin/users/users_list";
    }

    @GetMapping(value = {"/admin/users/add"})
    public String showAdminAddForm(Model model) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(model, profile);

        NewUserDTO formDTO = new NewUserDTO();

        List<OrganisationDTO> organisationList = organisationService.getAllOrganisation(profile);
        model.addAttribute("organisationList", organisationList);
        if (!profile.getOrganisationUid().equals(SYSTEM_ORG_UID)) {
            formDTO.setOrganisationUid(profile.getOrganisationUid());
        }

        List<ModuleDTO> moduleList = moduleService.getAllModules(profile);
        model.addAttribute("moduleList", moduleList);
        if (!profile.getRoles().equals(SecurityRole.ADMIN)) {
            formDTO.setModuleUid(profile.getModuleUid());
        }

        List<AssistantDTO> orgAssistants = assistantService.getAllAssistants(profile, false, false);
        model.addAttribute("assistantsList", orgAssistants);

        List<SecurityRole> securityRoles = new ArrayList<>(List.of(SecurityRole.values()));
        if (!profile.getOrganisationUid().equalsIgnoreCase(SYSTEM_ORG_UID)) {
            securityRoles.remove(SecurityRole.ADMIN);
        }
        if (profile.getRoles() == SecurityRole.MANAGER) {
            securityRoles.remove(SecurityRole.ORG_ADMIN);
        }
        model.addAttribute("securityRoles", securityRoles);
        model.addAttribute("formObject", formDTO);
        model.addAttribute("assignedAssistants", new ArrayList<String>());
        return "admin/users/users_insert";
    }

    @PostMapping(value = {"/admin/users/add"})
    public String processAdminAddForm(RedirectAttributes redirectAttributes,
                                      @Validated @ModelAttribute(name = "formObject") NewUserDTO form,
                                      @Validated @RequestParam(name = "assignedAssistants", required = false) List<String> assignedAssistants) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        redirectAttributes.addFlashAttribute("profileObject", profile);

        form.setAssistantUids(Objects.requireNonNullElseGet(assignedAssistants, ArrayList::new));
        OutputTool outputTool = usersService.createNewUser(profile, form);
        if (outputTool.getResult().equals(Result.EXCEPTION)) {
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.DANGER, outputTool.getComment())));
            return "redirect:/admin/users?added=false";
        }
        redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, "Users added successfully")));
        return "redirect:/admin/users";
    }

    @GetMapping(value = {"/admin/users/update"})
    public String showAdminUpdateForm(Model model,
                                      @RequestParam(name = "uid") String uid) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        model = usersService.addMandatoryAttributes(model, profile);

        UserProfileDTO updateProfile = usersService.getProfileFromUid(uid);
        model.addAttribute("formObject", updateProfile);

        List<AssistantDTO> userAssistants = assistantService.getAllUserAssistants(updateProfile.getAssistantId());

        List<AssistantDTO> orgAssistants = assistantService.getAllAssistants(profile, false, false);

        List<AssistantStateDTO> assistantStateDTOList = new ArrayList<>();

        for (AssistantDTO orgAssistant : orgAssistants) {
            AssistantStateDTO state = new AssistantStateDTO();
            state.setAssistantUid(orgAssistant.getUid());
            state.setAssistantName(orgAssistant.getName());
            boolean isAssigned = userAssistants.stream()
                    .anyMatch(userAssistant -> userAssistant.getUid().equals(orgAssistant.getUid()));
            state.setIsAssigned(isAssigned); // Set the assignment status based on match
            assistantStateDTOList.add(state);
        }
        model.addAttribute("assistantStateList", assistantStateDTOList);

        List<OrganisationDTO> organisationList = organisationService.getAllOrganisation(profile);
        model.addAttribute("organisationList", organisationList);

        List<ModuleDTO> moduleList = moduleService.getAllModules(profile);
        model.addAttribute("moduleList", moduleList);

        List<SecurityRole> securityRoles = new ArrayList<>(List.of(SecurityRole.values()));
        if (!profile.getOrganisationUid().equalsIgnoreCase(SYSTEM_ORG_UID)) {
            securityRoles.remove(SecurityRole.ADMIN);
        }
        if (profile.getRoles() == SecurityRole.MANAGER) {
            securityRoles.remove(SecurityRole.ORG_ADMIN);
        }
        model.addAttribute("securityRoles", securityRoles);
        model.addAttribute("assignedAssistants", new ArrayList<String>());
        return "admin/users/users_update";
    }

    @PostMapping(value = {"/admin/users/update"})
    public String processAdminUpdateForm(RedirectAttributes redirectAttributes,
                                         @Validated @ModelAttribute(name = "formObject") UserProfileDTO form,
                                         @Validated @RequestParam(name = "assignedAssistants", required = false) List<String> assignedAssistants) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(redirectAttributes, profile);

        if (form.getUid().equalsIgnoreCase(profile.getUid())) {
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.DANGER, "Unable to process action.")));
            return "redirect:/admin/users?updated=false";
        }
        OutputTool outputTool = usersService.updateProfile(form, assignedAssistants);
        redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, outputTool.getComment())));
        return "redirect:/admin/users";
    }

    @GetMapping(value = {"/admin/users/add/bulk"})
    public String showUserBulkForm(Model model) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(model, profile);
        model.addAttribute("userUid", profile.getUid());
        return "admin/users/users_bulk";
    }


    @PostMapping("/admin/users/add/bulk")
    public String processAddBulk(RedirectAttributes redirectAttributes,
                                 @RequestParam("uid") String usersUid,
                                 @RequestParam("formObject") MultipartFile file) {
        UserProfileDTO profile = usersService.getProfileFromUid(usersUid);
        redirectAttributes.addFlashAttribute("profileObject", profile);
        usersService.processCsv(profile, file);
        redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.INFO, "File uploaded, the results will be emailed to you.")));
        return "redirect:/admin/users";
    }

    @GetMapping(value = {"/admin/users/delete"})
    public String showAdminAddForm(RedirectAttributes redirectAttributes,
                                   @RequestParam("uid") String usersUid) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(redirectAttributes, profile);

        OutputTool outputTool = usersService.deleteProfile(usersUid);
        if (outputTool.getResult().equals(Result.EXCEPTION)) {
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.DANGER, outputTool.getComment())));
            return "redirect:/admin/users?deleted=false";
        }
        redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, outputTool.getComment())));
        return "redirect:/admin/users";
    }

}
