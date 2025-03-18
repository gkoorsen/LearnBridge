package africa.za.atech.spring.aio.functions.chats.dto;

import africa.za.atech.spring.aio.functions.assistant.database.model.Assistants;
import africa.za.atech.spring.aio.functions.chats.database.model.Chats;
import africa.za.atech.spring.aio.utils.HelperTools;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.gson.JsonElement;
import lombok.Data;
import lombok.SneakyThrows;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatsDTO {

    @JsonIgnore
    private String uid;
    @JsonIgnore
    private String assistantsUid;
    private String assistantsName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDateTime;
    @JsonIgnore
    private String createdBy;
    @JsonIgnore
    private String description;
    @JsonIgnore
    private LocalDateTime updatedDatetime;
    private String llmFirstMessageId;
    private String llmLastMessageId;
    private List<MessagesDTO> messages;

    private ChatsDTO(String uid, String assistantsUid, String assistantsName, LocalDateTime createdDateTime, String createdBy, String description, LocalDateTime updatedDatetime, String llmFirstMessageId, String llmLastMessageId, List<MessagesDTO> messages) {
        this.uid = uid;
        this.assistantsUid = assistantsUid;
        this.assistantsName = assistantsName;
        this.createdDateTime = createdDateTime;
        this.createdBy = createdBy;
        this.description = description;
        this.updatedDatetime = updatedDatetime;
        this.llmFirstMessageId = llmFirstMessageId;
        this.llmLastMessageId = llmLastMessageId;
        this.messages = messages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String uid;
        private String assistantsUid;
        private String assistantsName;
        private LocalDateTime createdDateTime;
        private String createdBy;
        private String description;
        private LocalDateTime updatedDatetime;
        private String llmFirstMessageId;
        private String llmLastMessageId;
        private final List<MessagesDTO> messageList = new ArrayList<>();


        Builder() {
        }

        @SneakyThrows
        public Builder withOpenAi(Assistants assistants, Chats chats) {
            return withOpenAi(assistants, chats, true);
        }

        @SneakyThrows
        public Builder withOpenAi(Assistants assistants, Chats chats, boolean toHtml) {
            this.uid = chats.getUid();
            this.assistantsUid = chats.getAssistantsUid();
            this.assistantsName = assistants.getName();
            this.createdDateTime = chats.getCreatedDateTime();
            this.createdBy = chats.getCreatedBy();
            this.description = chats.getDescription();
            this.updatedDatetime = chats.getUpdatedDatetime();
            this.llmFirstMessageId = chats.getLlmFirstMessageId();
            this.llmLastMessageId = chats.getLlmLastMessageId();
            for (JsonElement jsonElement : HelperTools.getJsonArray(chats.getLlmResponseObject(), "data")) {
                this.messageList.add(MessagesDTO.builder().withOpenAi(chats.getUid(), jsonElement.toString(), toHtml).build());
            }
            Collections.reverse(this.messageList);
            return this;
        }

        public ChatsDTO build() {
            return new ChatsDTO(this.uid, this.assistantsUid, this.assistantsName, this.createdDateTime, this.createdBy, this.description, this.updatedDatetime, this.llmFirstMessageId, this.llmLastMessageId, this.messageList);
        }
    }
}

