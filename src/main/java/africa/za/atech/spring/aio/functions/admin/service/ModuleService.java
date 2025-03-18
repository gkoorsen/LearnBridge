package africa.za.atech.spring.aio.functions.admin.service;

import africa.za.atech.spring.aio.functions.admin.database.model.Modules;
import africa.za.atech.spring.aio.functions.admin.database.model.Organisation;
import africa.za.atech.spring.aio.functions.admin.database.repo.ModuleRepo;
import africa.za.atech.spring.aio.functions.admin.database.repo.OrganisationRepo;
import africa.za.atech.spring.aio.functions.admin.dto.ModuleDTO;
import africa.za.atech.spring.aio.functions.admin.dto.UserProfileDTO;
import africa.za.atech.spring.aio.functions.admin.enums.SearchLevel;
import africa.za.atech.spring.aio.utils.HelperTools;
import africa.za.atech.spring.aio.utils.OutputTool;
import africa.za.atech.spring.aio.utils.enums.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static africa.za.atech.spring.aio.config.Initialize.SYSTEM_MOD_UID;
import static africa.za.atech.spring.aio.config.Initialize.SYSTEM_ORG_UID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleService {

    private final OrganisationRepo repoOrganisation;
    private final ModuleRepo repoModule;
    private final UsersService usersService;

    public List<ModuleDTO> getAllModules(UserProfileDTO profile) {
        if (profile.getOrganisationUid().equals(SYSTEM_ORG_UID)) {
            return repoModule.findAll()
                    .stream()
                    .map(mod -> new ModuleDTO().build(
                            repoOrganisation.findByUid(profile.getOrganisationUid()),
                            mod,
                            usersService.getProfiles(SearchLevel.MODULE, mod.getUid())))
                    .toList();
        }
        return getAllModules(profile.getOrganisationUid());
    }

    public List<ModuleDTO> getAllModules(String orgUid) {
        return repoModule.findAllByOrganisationUid(orgUid).stream()
                .map(mod -> new ModuleDTO().build(
                        repoOrganisation.findByUid(orgUid),
                        mod,
                        usersService.getProfiles(SearchLevel.MODULE, mod.getUid())))
                .toList();
    }

    public ModuleDTO getModule(String uid) {
        Modules modulesRecord = repoModule.findByUid(uid);
        Organisation orgRecord = repoOrganisation.findByUid(modulesRecord.getOrganisationUid());
        List<UserProfileDTO> userProfileDTOS = usersService.getProfiles(SearchLevel.MODULE, modulesRecord.getUid());
        return new ModuleDTO().build(orgRecord, modulesRecord, userProfileDTOS);
    }

    public OutputTool addModuleForOrg(ModuleDTO form) {
        Organisation orgRecord = repoOrganisation.findByUid(form.getOrganisationUid());
        if (orgRecord.getUid().equals(SYSTEM_ORG_UID)) {
            return new OutputTool(Result.EXCEPTION, "Unable to process your request.", null);
        }
        List<Modules> existingModules = repoModule.findAllByOrganisationUid(orgRecord.getUid());
        if (!existingModules.isEmpty()) {
            for (Modules d : existingModules) {
                if (d.getName().equalsIgnoreCase(form.getName())) {
                    return new OutputTool(Result.EXCEPTION, "Modules " + HelperTools.wrapVar(form.getName()) + " already exists for the " + HelperTools.wrapVar(orgRecord.getName()) + " organisation.", null);
                }
            }
        }
        Modules record = new Modules();
        record.setUid("m-atech-" + UUID.randomUUID());
        record.setOrganisationUid(orgRecord.getUid());
        record.setCreatedDateTime(LocalDateTime.now());
        record.setCreatedBy(HelperTools.getLoggedInUsername());
        record.setName(form.getName());
        record.setDisabled(false);
        repoModule.save(record);
        return new OutputTool(Result.SUCCESS, "Modules added successfully to the " + HelperTools.wrapVar(orgRecord.getName()) + " organisation.", record);
    }

    public OutputTool updateModuleForOrg(ModuleDTO form) {
        if (form.getUid().equalsIgnoreCase(SYSTEM_MOD_UID)) {
            return new OutputTool(Result.EXCEPTION, "Unable to process your request.", null);
        }
        Organisation orgRecord = repoOrganisation.findByUid(form.getOrganisationUid());
        List<Modules> existingModules = repoModule.findAllByOrganisationUid(orgRecord.getUid());
        if (!existingModules.isEmpty()) {
            for (Modules d : existingModules) {
                if (d.getName().equals(form.getName())) {
                    return new OutputTool(Result.EXCEPTION, "Modules " + HelperTools.wrapVar(form.getName()) + " already exists for the " + HelperTools.wrapVar(orgRecord.getName()) + " organisation.", null);
                }
            }
        }
        Modules record = repoModule.findByUid(form.getUid());
        record.setOrganisationUid(form.getOrganisationUid());
        record.setName(form.getName());
        record.setUpdateBy(HelperTools.getLoggedInUsername());
        record.setUpdateDatetime(LocalDateTime.now());
        repoModule.save(record);
        return new OutputTool(Result.SUCCESS, "Modules updated successfully.", null);
    }

    public OutputTool deleteModuleForOrg(String uid) {
        if (uid.equalsIgnoreCase(SYSTEM_MOD_UID)) {
            return new OutputTool(Result.EXCEPTION, "Unable to process your request.", null);
        }
        List<UserProfileDTO> userProfileDTOS = usersService.getProfiles(SearchLevel.MODULE, uid);
        if (!userProfileDTOS.isEmpty()) {
            return new OutputTool(Result.EXCEPTION, "Unable to delete. " + userProfileDTOS.size() + " users are assigned to this module. ", null);
        }
        repoModule.deleteAllByUid(uid);
        return new OutputTool(Result.SUCCESS, "Modules deleted successfully", null);
    }


}

