package africa.za.atech.spring.aio.functions.assistant;

import africa.za.atech.spring.aio.functions.admin.SecurityRole;
import africa.za.atech.spring.aio.functions.admin.dto.ModuleDTO;
import africa.za.atech.spring.aio.functions.admin.dto.OrganisationDTO;
import africa.za.atech.spring.aio.functions.admin.dto.UserProfileDTO;
import africa.za.atech.spring.aio.functions.admin.service.ModuleService;
import africa.za.atech.spring.aio.functions.admin.service.OrganisationService;
import africa.za.atech.spring.aio.functions.admin.service.UsersService;
import africa.za.atech.spring.aio.functions.assistant.dto.AssistantDTO;
import africa.za.atech.spring.aio.functions.assistant.enums.LlmType;
import africa.za.atech.spring.aio.utils.Alert;
import africa.za.atech.spring.aio.utils.HelperTools;
import africa.za.atech.spring.aio.utils.OutputTool;
import africa.za.atech.spring.aio.utils.enums.AlertType;
import africa.za.atech.spring.aio.utils.enums.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

import static africa.za.atech.spring.aio.config.Initialize.SYSTEM_ORG_UID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AssistantController {

    @Value("${atech.app.datadump.assistant.chats.permission.prompt}")
    private String assistantChatDisclaimer;

    private final OrganisationService organisationService;
    private final ModuleService moduleService;
    private final UsersService usersService;
    private final AssistantService assistantService;

    @GetMapping(value = {"/admin/assistants"})
    public String showAssistantsHome(Model model) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(model, profile);

        List<AssistantDTO> recordsList;
        if (profile.getRoles().equals(SecurityRole.MANAGER)) {
            recordsList = assistantService.getAllAssistants(profile, false, true);
            if (!recordsList.isEmpty()) {
                recordsList.removeIf(record -> !record.getModuleUid().equals(profile.getModuleUid()));
            }
        } else {
            recordsList = assistantService.getAllAssistants(profile, true, true);
        }

        model.addAttribute("recordList", recordsList);
        model.addAttribute("downloadDisclaimer", assistantChatDisclaimer);
        return "admin/assistants/assistant_list";
    }

    @GetMapping(value = {"/admin/assistants/insert"})
    public String getAssistantAddForm(Model model) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(model);

        AssistantDTO formObject = new AssistantDTO();
        List<OrganisationDTO> organisationList = organisationService.getAllOrganisation(profile);
        model.addAttribute("organisationList", organisationList);
        if (!profile.getOrganisationUid().equals(SYSTEM_ORG_UID)) {
            formObject.setOrganisationUid(profile.getOrganisationUid());
        }
        List<ModuleDTO> moduleList = moduleService.getAllModules(profile);
        model.addAttribute("moduleList", moduleList);
        if (!profile.getRoles().equals(SecurityRole.ADMIN)) {
            formObject.setModuleUid(profile.getModuleUid());
        }
        List<LlmType> llmTypes = new ArrayList<>(List.of(LlmType.values()));
        model.addAttribute("llmTypeList", llmTypes);

        model.addAttribute("formObject", formObject);
        return "admin/assistants/assistant_insert";
    }

    @PostMapping(value = {"/admin/assistants"})
    public String processAssistantAddForm(Model model, RedirectAttributes redirectAttributes,
                                          @Validated @ModelAttribute(name = "formObject") AssistantDTO form) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        OutputTool outputTool = assistantService.insertAssistant(form);
        if (!outputTool.getResult().equals(Result.SUCCESS)) {
            usersService.addMandatoryAttributes(model, profile);

            List<OrganisationDTO> organisationList = organisationService.getAllOrganisation(profile);
            model.addAttribute("organisationList", organisationList);
            if (!profile.getOrganisationUid().equals(SYSTEM_ORG_UID)) {
                form.setOrganisationUid(profile.getOrganisationUid());
            }
            List<ModuleDTO> moduleList = moduleService.getAllModules(profile);
            model.addAttribute("moduleList", moduleList);
            if (!profile.getRoles().equals(SecurityRole.ADMIN)) {
                form.setModuleUid(profile.getModuleUid());
            }
            List<LlmType> llmTypes = new ArrayList<>(List.of(LlmType.values()));
            model.addAttribute("llmTypeList", llmTypes);

            model.addAttribute("formObject", form);
            model.addAttribute("alertList", List.of(new Alert(AlertType.DANGER, outputTool.getComment())));
            return "admin/assistants/assistant_insert";
        }
        usersService.addMandatoryAttributes(redirectAttributes, profile);
        redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, outputTool.getComment())));
        return "redirect:/admin/assistants";
    }

    @GetMapping(value = {"/admin/assistants/update"})
    public String getAssistantUpdateForm(Model model,
                                         @RequestParam(name = "uid") String assistantUid) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(model, profile);

        List<ModuleDTO> moduleList = moduleService.getAllModules(profile);
        model.addAttribute("moduleList", moduleList);

        AssistantDTO formObject = (AssistantDTO) assistantService.getAssistant(assistantUid).getObject();
        formObject.setLlmApiKey("masked");
        model.addAttribute("formObject", formObject);
        return "admin/assistants/assistant_update";
    }

    @PostMapping(value = {"/admin/assistants/update"})
    public String processAssistantUpdateForm(Model model, RedirectAttributes redirectAttributes,
                                             @Validated @ModelAttribute(name = "formObject") AssistantDTO form) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        OutputTool outputTool = assistantService.updateAssistant(form);
        if (outputTool.getResult().equals(Result.EXCEPTION)) {
            usersService.addMandatoryAttributes(model, profile);
            model.addAttribute("formObject", form);
            model.addAttribute("alertList", List.of(new Alert(AlertType.DANGER, outputTool.getComment())));
            return "admin/assistants/assistant_update";
        } else {
            usersService.addMandatoryAttributes(redirectAttributes, profile);
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, outputTool.getComment())));
            return "redirect:/admin/assistants";
        }
    }

    @GetMapping("/admin/assistants/delete")
    public String deleteChat(RedirectAttributes redirectAttributes,
                             @RequestParam(value = "uid") String uid) {
        usersService.addMandatoryAttributes(redirectAttributes);
        OutputTool outputTool = assistantService.deleteAssistant(uid);
        if (outputTool.getResult().equals(Result.EXCEPTION)) {
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.DANGER, outputTool.getComment())));
            return "redirect:/admin/assistants?deleted=false";
        }
        redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, outputTool.getComment())));
        return "redirect:/admin/assistants";
    }

    @GetMapping("/admin/assistants/export")
    public ResponseEntity<byte[]> exportChats(
            @RequestParam(name = "uid") String assistantUid) throws JsonProcessingException {
        return assistantService.downloadChatsForAssistant(assistantUid);
    }

}
