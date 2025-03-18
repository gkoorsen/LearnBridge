package africa.za.atech.spring.aio.functions.admin.service;

import africa.za.atech.spring.aio.functions.admin.database.model.Organisation;
import africa.za.atech.spring.aio.functions.admin.database.repo.OrganisationRepo;
import africa.za.atech.spring.aio.functions.admin.dto.ModuleDTO;
import africa.za.atech.spring.aio.functions.admin.dto.OrganisationDTO;
import africa.za.atech.spring.aio.functions.admin.dto.OrganisationMetaDTO;
import africa.za.atech.spring.aio.functions.admin.dto.UserProfileDTO;
import africa.za.atech.spring.aio.functions.admin.enums.SearchLevel;
import africa.za.atech.spring.aio.functions.assistant.AssistantService;
import africa.za.atech.spring.aio.functions.chats.ChatService;
import africa.za.atech.spring.aio.functions.chats.dto.ChatsDTO;
import africa.za.atech.spring.aio.utils.HelperTools;
import africa.za.atech.spring.aio.utils.OutputTool;
import africa.za.atech.spring.aio.utils.enums.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static africa.za.atech.spring.aio.config.Initialize.SYSTEM_ORG_NAME;
import static africa.za.atech.spring.aio.config.Initialize.SYSTEM_ORG_UID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepo repoOrganisation;
    private final ModuleService moduleService;
    private final UsersService usersService;
    private final AssistantService assistantService;
    private final ChatService chatService;

    public List<OrganisationDTO> getAllOrganisation(UserProfileDTO profile) {
        if (profile.getOrganisationUid().equals(SYSTEM_ORG_UID)) {
            return repoOrganisation.findAll()
                    .stream()
                    .map(this::getOrganisation)
                    .collect(Collectors.toList());
        }
        return repoOrganisation.findAll()
                .stream()
                .filter(org -> org.getUid().equals(profile.getOrganisationUid()))
                .map(this::getOrganisation)
                .collect(Collectors.toList());
    }

    public OrganisationDTO getOrganisation(String orgUid) {
        return getOrganisation(repoOrganisation.findByUid(orgUid));
    }

    private OrganisationDTO getOrganisation(Organisation record) {
        OrganisationMetaDTO meta = new OrganisationMetaDTO();
        meta.setListOfModules(moduleService.getAllModules(record.getUid()));
        meta.setListOfAssistants(assistantService.getAllAssistants(record.getUid()));
        meta.setListOfUsers(usersService.getProfiles(SearchLevel.ORGANISATION, record.getUid()));
        int chatsCount = 0;
        for (UserProfileDTO user : meta.getListOfUsers()) {
            List<ChatsDTO> chatsList = chatService.getUserChats(user.getUid());
            chatsCount = chatsCount + chatsList.size();
        }
        meta.setModuleCount(meta.getListOfModules().size());
        meta.setAssistantsCount(meta.getListOfAssistants().size());
        meta.setUsersCount(meta.getListOfUsers().size());
        meta.setChatsCount(chatsCount);

        if (meta.getModuleCount() == 0 && meta.getAssistantsCount() == 0 && meta.getUsersCount() == 0) {
            meta.setDelete(true);
        }
        return new OrganisationDTO().build(record, meta);
    }

    public OutputTool addOrganisation(OrganisationDTO form) {
        if (form.getName().equalsIgnoreCase(SYSTEM_ORG_NAME)) {
            return new OutputTool(Result.EXCEPTION, "Unable to process request.", null);
        }

        Optional<Organisation> lookup = repoOrganisation.findByNameIgnoreCase(form.getName());
        if (lookup.isPresent()) {
            return new OutputTool(Result.EXCEPTION, "Organisation " + HelperTools.wrapVar(form.getName()) + " already exists.", null);
        }
        Organisation record = new Organisation();
        record.setUid("o-atech-" + UUID.randomUUID());
        record.setCreatedDateTime(LocalDateTime.now());
        record.setCreatedBy(HelperTools.getLoggedInUsername());
        record.setName(form.getName().trim());
        record.setDisabled(false);
        repoOrganisation.save(record);
        return new OutputTool(Result.SUCCESS, "Organisation added successfully.", new OrganisationDTO().build(record, new OrganisationMetaDTO()));
    }

    public OutputTool updateOrganisation(OrganisationDTO form) {
        if (form.getName().equalsIgnoreCase(SYSTEM_ORG_NAME)) {
            return new OutputTool(Result.EXCEPTION, "Unable to process request.", null);
        }

        Optional<Organisation> lookup = repoOrganisation.findByNameIgnoreCase(form.getName());
        if (lookup.isPresent()) {
            return new OutputTool(Result.EXCEPTION, "Organisation " + HelperTools.wrapVar(form.getName()) + " already exists.", null);
        }
        Organisation record = repoOrganisation.findByUid(form.getUid());
        record.setName(form.getName());
        record.setUpdateBy(HelperTools.getLoggedInUsername());
        record.setUpdateDatetime(LocalDateTime.now());
        repoOrganisation.save(record);
        return new OutputTool(Result.SUCCESS, "Organisation updated successfully.", null);
    }

    public OutputTool deleteOrganisation(String uid) {
        if (uid.equalsIgnoreCase(SYSTEM_ORG_UID)) {
            return new OutputTool(Result.EXCEPTION, "Unable to process request.", null);
        }
        Organisation organisationRecord = repoOrganisation.findByUid(uid);
        List<ModuleDTO> mods = moduleService.getAllModules(organisationRecord.getUid());
        if (!mods.isEmpty()) {
            return new OutputTool(Result.EXCEPTION, "Unable to delete organisation. Modules need to be deleted first.", null);
        }
        repoOrganisation.delete(organisationRecord);
        return new OutputTool(Result.SUCCESS, "Organisation deleted successfully.", null);
    }

}

