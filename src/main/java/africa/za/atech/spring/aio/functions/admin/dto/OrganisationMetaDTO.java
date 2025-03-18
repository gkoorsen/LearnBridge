package africa.za.atech.spring.aio.functions.admin.dto;

import africa.za.atech.spring.aio.functions.assistant.dto.AssistantDTO;
import lombok.Data;

import java.util.List;

@Data
public class OrganisationMetaDTO {

    private List<ModuleDTO> listOfModules;
    private List<AssistantDTO> listOfAssistants;
    private List<UserProfileDTO> listOfUsers;
    private int moduleCount;
    private int assistantsCount;
    private int usersCount;
    private int chatsCount;
    private boolean delete;

}

