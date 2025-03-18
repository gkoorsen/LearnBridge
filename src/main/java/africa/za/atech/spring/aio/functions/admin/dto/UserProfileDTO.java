package africa.za.atech.spring.aio.functions.admin.dto;

import africa.za.atech.spring.aio.functions.admin.SecurityRole;
import africa.za.atech.spring.aio.functions.admin.database.model.Modules;
import africa.za.atech.spring.aio.functions.admin.database.model.Organisation;
import africa.za.atech.spring.aio.functions.admin.database.model.Users;
import africa.za.atech.spring.aio.functions.assistant.dto.AssistantDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileDTO {

    private String uid;
    private LocalDateTime createdDateTime;
    private String organisationUid;
    private String organisationName;
    private String moduleUid;
    private String moduleName;
    private String username;
    private String maskedId;
    private String name;
    private String surname;
    private String emailAddress;
    private String password;
    private Boolean disabled;
    private SecurityRole roles;
    private String newPassword;
    private String confirmPassword;
    private String assistantId;
    private List<AssistantDTO> assistantDTOList;

    public UserProfileDTO build(Organisation organisation, Modules modules, Users record, List<AssistantDTO> assistantDTOList) {
        this.uid = record.getUid();
        this.createdDateTime = record.getCreatedDateTime();
        this.organisationUid = record.getOrganisationUid();
        this.organisationName = organisation.getName();
        this.moduleUid = record.getModuleUid();
        this.moduleName = modules.getName();
        this.username = record.getUsername();
        this.maskedId = record.getUid();
        this.name = record.getName();
        this.surname = record.getSurname();
        this.emailAddress = record.getEmailAddress();
        this.password = "";
        this.disabled = record.getDisabled();
        this.roles = SecurityRole.valueOf(record.getRole());
        this.newPassword = "";
        this.confirmPassword = "";
        this.assistantId = record.getAssistantsUids();
        this.assistantDTOList = assistantDTOList;
        return this;
    }
}
