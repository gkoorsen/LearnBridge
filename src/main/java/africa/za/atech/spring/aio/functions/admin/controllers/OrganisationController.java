package africa.za.atech.spring.aio.functions.admin.controllers;

import africa.za.atech.spring.aio.config.Initialize;
import africa.za.atech.spring.aio.functions.admin.dto.OrganisationDTO;
import africa.za.atech.spring.aio.functions.admin.dto.UserProfileDTO;
import africa.za.atech.spring.aio.functions.admin.service.OrganisationService;
import africa.za.atech.spring.aio.functions.admin.service.UsersService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrganisationController {

    private final UsersService usersService;
    private final OrganisationService organisationService;


    @GetMapping(value = {"/admin/organisation"})
    public String showOrgHome(Model model) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(model, profile);

        List<OrganisationDTO> organisationDTOList = organisationService.getAllOrganisation(profile);
        model.addAttribute("organisationList", organisationDTOList);
        return "admin/organisation/organisation_list";
    }

    @GetMapping(value = {"/admin/organisation/add"})
    public String showOrgInsertForm(Model model) {
        model = usersService.addMandatoryAttributes(model);
        model.addAttribute("formObject", new OrganisationDTO());
        return "admin/organisation/organisation_insert";
    }

    @PostMapping(value = {"/admin/organisation"})
    public String processOrgInsertForm(RedirectAttributes redirectAttributes,
                                       @Validated @ModelAttribute(name = "formObject") OrganisationDTO form) {
        redirectAttributes = usersService.addMandatoryAttributes(redirectAttributes);

        OutputTool outputTool = organisationService.addOrganisation(form);
        if (outputTool.getResult().equals(Result.EXCEPTION)) {
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.DANGER, outputTool.getComment())));
            return "redirect:/admin/organisation";
        }
        redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, outputTool.getComment())));
        return "redirect:/admin/organisation";
    }

    @GetMapping(value = {"/admin/organisation/update"})
    public String showOrgUpdateForm(Model model,
                                    @RequestParam(name = "uid") String uid) {
        model = usersService.addMandatoryAttributes(model);
        model.addAttribute("formObject", organisationService.getOrganisation(uid));
        return "admin/organisation/organisation_update";
    }

    @PostMapping(value = {"/admin/organisation/update"})
    public String processOrgUpdateForm(RedirectAttributes redirectAttributes,
                                       @Validated @ModelAttribute(name = "formObject") OrganisationDTO form) {
        redirectAttributes = usersService.addMandatoryAttributes(redirectAttributes);

        if (form.getUid().equals(Initialize.SYSTEM_ORG_UID)) {
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.DANGER, "Unable to process request.")));
            return "redirect:/admin/organisation?update=false";
        }

        OutputTool outputTool = organisationService.updateOrganisation(form);
        if (outputTool.getResult().equals(Result.EXCEPTION)) {
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.DANGER, outputTool.getComment())));
            return "redirect:/admin/organisation";
        }
        redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, outputTool.getComment())));
        return "redirect:/admin/organisation";
    }

    @GetMapping(value = {"/admin/organisation/delete"})
    public String processOrgDelete(RedirectAttributes redirectAttributes,
                                   @RequestParam(name = "uid") String maskedId) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(redirectAttributes, profile);

        OutputTool outputTool = organisationService.deleteOrganisation(maskedId);
        redirectAttributes.addFlashAttribute("recordList", organisationService.getAllOrganisation(profile));
        if (outputTool.getResult().equals(Result.EXCEPTION)) {
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.DANGER, outputTool.getComment())));
            return "redirect:/admin/organisation";
        }
        redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, outputTool.getComment())));
        return "redirect:/admin/organisation";
    }


}
