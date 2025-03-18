package africa.za.atech.spring.aio.functions.assistant.dto;

import africa.za.atech.spring.aio.functions.assistant.database.model.Assistants;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
public class AssistantDTO {

    private String uid;
    private String organisationUid;
    private String moduleUid;
    private LocalDateTime createdDateTime;
    private String createdBy;
    private String llmType;
    private String name;
    private String description;
    private String additionalInstructions;
    private String llmOrganisationId;
    private String llmAssistantId;
    private String llmApiKey;
    private Boolean disabled;
    private LocalDateTime updatedDatetime;
    private String updatedBy;
    private String disabledBy;
    private LocalDateTime disabledDatetime;
    private Integer chatCount;

    public AssistantDTO build(Assistants assistants) {
        this.uid = assistants.getUid();
        this.organisationUid = assistants.getOrganisationUid();
        this.moduleUid = assistants.getModuleUid();
        this.createdDateTime = assistants.getCreatedDateTime();
        this.createdBy = assistants.getCreatedBy();
        this.llmType = assistants.getLlmType();
        this.name = assistants.getName().trim();
        this.description = assistants.getDescription().trim();
        this.additionalInstructions = Optional.ofNullable(assistants.getAdditionalInstructions()).map(String::trim).orElse("");
        this.llmOrganisationId = assistants.getLlmOrganisationId().trim();
        this.llmAssistantId = assistants.getLlmAssistantId().trim();
        this.llmApiKey = assistants.getLlmApiKey().trim();
        this.disabled = assistants.getDisabled();
        this.updatedDatetime = assistants.getUpdatedDatetime();
        this.updatedBy = assistants.getUpdatedBy();
        this.disabledBy = assistants.getDisabledBy();
        this.disabledDatetime = assistants.getDisabledDatetime();
        return this;
    }

    public AssistantDTO build(Assistants assistants, int chatCount) {
        this.setChatCount(chatCount);
        return build(assistants);
    }

}
