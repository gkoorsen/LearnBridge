package africa.za.atech.spring.aio.functions.chats.database.model;

import africa.za.atech.spring.aio.utils.HelperTools;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Transactional
@Table(schema = "public", name = "chats")
public class Chats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid")
    private String uid;

    @Column(name = "assistants_uid")
    private String assistantsUid;

    @Column(name = "created_datetime")
    private LocalDateTime createdDateTime;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "description")
    private String description;

    @Column(name = "updated_datetime")
    private LocalDateTime updatedDatetime;

    @Column(name = "llm_assistant_id")
    private String llmAssistantId;

    @Column(name = "llm_thread_id")
    private String llmThreadId;

    @Column(name = "llm_first_message_id")
    private String llmFirstMessageId;

    @Column(name = "llm_last_message_id")
    private String llmLastMessageId;

    @Column(name = "llm_response_object")
    private String llmResponseObject;

    public Chats buildInsert(
            LocalDateTime createdDateTime,
            String assistantUid,
            String description,
            String llmAssistantId,
            String llmThreadId,
            String llmFirstMessageId,
            String llmLastMessageId,
            String llmResponseObject
    ) {
        this.uid = "c-" + UUID.randomUUID();
        this.assistantsUid = assistantUid;
        this.createdDateTime = createdDateTime;
        this.createdBy = HelperTools.getLoggedInUsername();
        this.description = description;
        this.llmAssistantId = llmAssistantId;
        this.llmThreadId = llmThreadId;
        this.llmFirstMessageId = llmFirstMessageId;
        this.llmLastMessageId = llmLastMessageId;
        this.llmResponseObject = llmResponseObject;
        return this;
    }
}

