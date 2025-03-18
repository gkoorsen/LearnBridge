package africa.za.atech.spring.aio.functions.assistant;

import africa.za.atech.spring.aio.functions.admin.dto.UserProfileDTO;
import africa.za.atech.spring.aio.functions.assistant.database.model.Assistants;
import africa.za.atech.spring.aio.functions.assistant.database.repo.AssistantsRepo;
import africa.za.atech.spring.aio.functions.assistant.dto.AssistantDTO;
import africa.za.atech.spring.aio.functions.chats.database.repo.ChatsRepo;
import africa.za.atech.spring.aio.functions.chats.dto.ChatsDTO;
import africa.za.atech.spring.aio.utils.HelperTools;
import africa.za.atech.spring.aio.utils.OutputTool;
import africa.za.atech.spring.aio.utils.enums.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static africa.za.atech.spring.aio.config.Initialize.SYSTEM_ORG_UID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantService {

    private final AssistantsRepo repoAssistants;
    private final ChatsRepo repoChats;

    public List<AssistantDTO> getAllAssistants(UserProfileDTO profile, Boolean includeDisabled, Boolean includeChatCount) {
        if (profile.getOrganisationUid().equalsIgnoreCase(SYSTEM_ORG_UID)) {
            if (includeChatCount) {
                return repoAssistants.findAll().stream().map(assistants -> new AssistantDTO().build(assistants, repoChats.findAllByAssistantsUid(assistants.getUid()).size())).collect(Collectors.toList());
            }
            return repoAssistants.findAll().stream().map(assistants -> new AssistantDTO().build(assistants)).collect(Collectors.toList());
        } else {
            if (includeDisabled) {
                if (includeChatCount) {
                    return repoAssistants.findAllByOrganisationUid(profile.getOrganisationUid()).stream().map(assistants -> new AssistantDTO().build(assistants, repoChats.findAllByAssistantsUid(assistants.getUid()).size())).collect(Collectors.toList());
                } else {
                    return repoAssistants.findAllByOrganisationUid(profile.getOrganisationUid()).stream().map(assistants -> new AssistantDTO().build(assistants)).collect(Collectors.toList());
                }
            } else {
                if (includeChatCount) {
                    return repoAssistants.findAllByOrganisationUidAndDisabledIsFalse(profile.getOrganisationUid()).stream().map(assistants -> new AssistantDTO().build(assistants, repoChats.findAllByAssistantsUid(assistants.getUid()).size())).collect(Collectors.toList());
                } else {
                    return repoAssistants.findAllByOrganisationUidAndDisabledIsFalse(profile.getOrganisationUid()).stream().map(assistants -> new AssistantDTO().build(assistants)).collect(Collectors.toList());
                }
            }
        }
    }

    public List<AssistantDTO> getAllAssistants(String orgUid) {
        return repoAssistants.findAllByOrganisationUid(orgUid).stream()
                .map(assistants -> new AssistantDTO().build(assistants)).toList();
    }

    /**
     * @return On success, it returns and output with the Assistants record object
     */
    public OutputTool getAssistant(String uid) {
        Assistants rec = repoAssistants.findByUid(uid);
        return new OutputTool(Result.SUCCESS, "", new AssistantDTO().build(rec));
    }

    public List<AssistantDTO> getAllUserAssistants(String assistantUidList) {
        List<AssistantDTO> userAssistants = new ArrayList<>();
        List<String> idList = Arrays.stream(assistantUidList.substring(1, assistantUidList.length() - 1).split(","))
                .map(String::trim)
                .toList();
        for (String assistantUid : idList) {
            Optional<Assistants> rec = repoAssistants.findByUidAndDisabledIsFalse(assistantUid);
            rec.ifPresent(assistants -> userAssistants.add(new AssistantDTO().build(assistants)));
        }
        return userAssistants;
    }

    public OutputTool insertAssistant(AssistantDTO form) {
        Optional<Assistants> lookupRecord = repoAssistants.findByOrganisationUidAndNameIgnoreCase(form.getOrganisationUid(), form.getName());
        if (lookupRecord.isPresent()) {
            if (lookupRecord.get().getName().equalsIgnoreCase(form.getName())) {
                return new OutputTool(Result.EXCEPTION, "Assistant with name: " + HelperTools.wrapVar(form.getName()) + " already exists.", form);
            }
        }
        Assistants rec = new Assistants();
        rec.setUid("a-atech-" + UUID.randomUUID());
        rec.setOrganisationUid(form.getOrganisationUid());
        rec.setModuleUid(form.getModuleUid());
        rec.setCreatedDateTime(LocalDateTime.now());
        rec.setCreatedBy(HelperTools.getLoggedInUsername());
        rec.setLlmType(form.getLlmType());
        rec.setName(form.getName());
        rec.setDescription(form.getDescription());
        rec.setAdditionalInstructions(Optional.ofNullable(form.getAdditionalInstructions()).map(String::trim).orElse(""));
        rec.setLlmOrganisationId(form.getLlmOrganisationId().trim());
        rec.setLlmAssistantId(form.getLlmAssistantId().trim());
        rec.setLlmApiKey(form.getLlmApiKey().trim());
        rec.setDisabled(false);
        repoAssistants.save(rec);
        return new OutputTool(Result.SUCCESS, "Assistant with name: " + HelperTools.wrapVar(rec.getName()) + " has been inserted successfully.", null);
    }

    public OutputTool updateAssistant(AssistantDTO form) {
        Assistants rec = repoAssistants.findByUid(form.getUid());

        if (!rec.getName().trim().equalsIgnoreCase(form.getName().trim())) {
            Optional<Assistants> nameCheck = repoAssistants.findByOrganisationUidAndNameIgnoreCase(form.getOrganisationUid(), form.getName().trim());
            if (nameCheck.isPresent()) {
                return new OutputTool(Result.EXCEPTION, "Assistant with name: " + HelperTools.wrapVar(rec.getName()) + " already exists.", null);
            } else {
                rec.setName(form.getName().trim());
            }
        }
        rec.setModuleUid(form.getModuleUid());
        rec.setDescription(form.getDescription().trim());
        if (!form.getLlmApiKey().equalsIgnoreCase("masked")) {
            rec.setLlmApiKey(form.getLlmApiKey().trim());
        }
        rec.setAdditionalInstructions(form.getAdditionalInstructions().trim());
        rec.setDisabled(form.getDisabled());
        rec.setUpdatedBy(HelperTools.getLoggedInUsername());
        rec.setUpdatedDatetime(LocalDateTime.now());
        repoAssistants.save(rec);
        return new OutputTool(Result.SUCCESS, "Assistant with name: " + HelperTools.wrapVar(rec.getName()) + " has been updated successfully.", null);
    }

    public OutputTool deleteAssistant(String uid) {
        Assistants assistants = repoAssistants.findByUid(uid);
        repoChats.deleteAllByAssistantsUid(assistants.getUid());
        repoAssistants.delete(assistants);
        return new OutputTool(Result.SUCCESS, "Assistant deleted successfully", null);
    }

    public ResponseEntity<byte[]> downloadChatsForAssistant(String uid) throws JsonProcessingException {
        Assistants assistants = repoAssistants.findByUid(uid);
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = assistants.getName() + "_" + date + ".json";

        List<ChatsDTO> jsonOutput = repoChats.findAllByAssistantsUid(uid)
                .stream()
                .map(chats -> ChatsDTO.builder().withOpenAi(assistants, chats, false).build())
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsBytes(jsonOutput));
    }

}
