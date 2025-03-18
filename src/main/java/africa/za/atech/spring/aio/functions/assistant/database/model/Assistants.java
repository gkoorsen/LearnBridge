package africa.za.atech.spring.aio.functions.assistant.database.model;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Transactional
@Table(schema = "public", name = "assistants")
public class Assistants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid")
    private String uid;

    @Column(name = "organisation_uid")
    private String organisationUid;

    @Column(name = "module_uid")
    private String moduleUid;

    @Column(name = "created_datetime")
    private LocalDateTime createdDateTime;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "llm_type")
    private String llmType;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "additional_instructions")
    private String additionalInstructions;

    @Column(name = "llm_organisation_id")
    private String llmOrganisationId;

    @Column(name = "llm_assistant_id")
    private String llmAssistantId;

    @Column(name = "llm_api_key")
    private String llmApiKey;

    @Column(name = "disabled")
    private Boolean disabled;

    @Column(name = "updated_datetime")
    private LocalDateTime updatedDatetime;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "disabled_by")
    private String disabledBy;

    @Column(name = "disabled_datetime")
    private LocalDateTime disabledDatetime;

}

