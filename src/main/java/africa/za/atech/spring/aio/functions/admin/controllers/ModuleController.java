package africa.za.atech.spring.aio.functions.admin.controllers;

import africa.za.atech.spring.aio.config.Initialize;
import africa.za.atech.spring.aio.functions.admin.dto.ModuleDTO;
import africa.za.atech.spring.aio.functions.admin.dto.OrganisationDTO;
import africa.za.atech.spring.aio.functions.admin.dto.UserProfileDTO;
import africa.za.atech.spring.aio.functions.admin.service.ModuleService;
import africa.za.atech.spring.aio.functions.admin.service.OrganisationService;
import africa.za.atech.spring.aio.functions.admin.service.UsersService;
import africa.za.atech.spring.aio.utils.Alert;
import africa.za.atech.spring.aio.utils.HelperTools;
import africa.za.atech.spring.aio.utils.OutputTool;
import africa.za.atech.spring.aio.utils.enums.AlertType;
import africa.za.atech.spring.aio.utils.enums.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;
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
public class ModuleController {

    private final UsersService usersService;
    private final OrganisationService organisationService;
    private final ModuleService moduleService;

    @GetMapping(value = {"/admin/module"})
    public String showModules(Model model,
                              @RequestParam(name = "uid") String orgUid) {
        model = usersService.addMandatoryAttributes(model);
        OrganisationDTO organisation = organisationService.getOrganisation(orgUid);
        model.addAttribute("orgName", WordUtils.capitalizeFully(organisation.getName()));
        model.addAttribute("orgUid", organisation.getUid());
        model.addAttribute("recordList", organisation.getOrganisationMetaDTO().getListOfModules());
        return "admin/organisation/module_list";
    }

    @GetMapping(value = {"/admin/module/add"})
    public String showInsertModuleForm(Model model,
                                       @RequestParam(name = "uid") String orgUid) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        model = usersService.addMandatoryAttributes(model, profile);

        OrganisationDTO org = organisationService.getOrganisation(orgUid);
        List<OrganisationDTO> organisationList = organisationService.getAllOrganisation(profile);
        model.addAttribute("organisationList", organisationList);

        ModuleDTO form = new ModuleDTO();
        form.setOrganisationUid(org.getUid());
        form.setOrganisationName(org.getName());
        model.addAttribute("formObject", form);
        return "admin/organisation/module_insert";
    }

    @PostMapping(value = {"/admin/module/add"})
    public String processModuleInsertForm(RedirectAttributes redirectAttributes,
                                          @Validated @ModelAttribute(name = "formObject") ModuleDTO form) {
        redirectAttributes = usersService.addMandatoryAttributes(redirectAttributes);

        if (form.getOrganisationUid().equals(Initialize.SYSTEM_ORG_UID)) {
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.DANGER, "Unable to process request")));
            return "redirect:/admin/module?uid=" + form.getOrganisationUid();
        }

        OutputTool outputTool = moduleService.addModuleForOrg(form);
        if (outputTool.getResult().equals(Result.EXCEPTION)) {
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.DANGER, outputTool.getComment())));
            return "redirect:/admin/module?uid=" + form.getOrganisationUid();
        }
        redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, outputTool.getComment())));
        return "redirect:/admin/module?uid=" + form.getOrganisationUid();
    }

    @GetMapping(value = {"/admin/module/update"})
    public String showModuleUpdateForm(Model model,
                                       @RequestParam(name = "uid") String moduleUid) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        model = usersService.addMandatoryAttributes(model, profile);

        List<OrganisationDTO> organisationList = organisationService.getAllOrganisation(profile);
        model.addAttribute("organisationList", organisationList);

        ModuleDTO form = moduleService.getModule(moduleUid);
        model.addAttribute("formObject", form);
        return "admin/organisation/module_update";
    }

    @PostMapping(value = {"/admin/module/update"})
    public String processModuleUpdateForm(RedirectAttributes redirectAttributes,
                                          @Validated @ModelAttribute(name = "formObject") ModuleDTO form) {
        redirectAttributes = usersService.addMandatoryAttributes(redirectAttributes);
        OutputTool outputTool = moduleService.updateModuleForOrg(form);
        if (outputTool.getResult().equals(Result.EXCEPTION)) {
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.DANGER, outputTool.getComment())));
        } else {
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, outputTool.getComment())));
        }
        return "redirect:/admin/module?uid=" + form.getOrganisationUid();
    }

    @GetMapping(value = {"/admin/module/delete"})
    public String processDeleteModule(RedirectAttributes redirectAttributes,
                                      @RequestParam(name = "ouid") String orgUid,
                                      @RequestParam(name = "moduleUid") String moduleUid) {
        redirectAttributes = usersService.addMandatoryAttributes(redirectAttributes);
        OutputTool outputTool = moduleService.deleteModuleForOrg(moduleUid);
        if (outputTool.getResult().equals(Result.EXCEPTION)) {
            redirectAttributes.addFlashAttribute(List.of(new Alert(AlertType.DANGER, outputTool.getComment())));
        } else {
            redirectAttributes.addFlashAttribute(List.of(new Alert(AlertType.SUCCESS, outputTool.getComment())));
        }
        return "redirect:/admin/module?uid=" + orgUid;
    }


}
