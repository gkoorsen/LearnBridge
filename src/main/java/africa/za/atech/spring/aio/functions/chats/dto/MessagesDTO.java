package africa.za.atech.spring.aio.functions.chats.dto;

import africa.za.atech.spring.aio.config.Initialize;
import africa.za.atech.spring.aio.functions.assistant.enums.LlmType;
import africa.za.atech.spring.aio.utils.HelperTools;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.jayway.jsonpath.JsonPath;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MessagesDTO {

    @JsonIgnore
    private String chatUid;
    private LlmType llmType;
    @JsonIgnore
    private String LlmAssistantId;
    private String llmThreadId;
    private String llmRunId;
    private String llmMessageId;
    private String role;
    private String text;
    private Long createdTimeStamp;
    private String createdDateTime;

    private MessagesDTO(String chatUid, LlmType llmType, String LlmAssistantId, String llmThreadId, String llmRunId, String llmMessageId, String role, String text, Long createdTimeStamp) {
        this.chatUid = chatUid;
        this.llmType = llmType;
        this.LlmAssistantId = LlmAssistantId;
        this.llmThreadId = llmThreadId;
        this.llmRunId = llmRunId;
        this.llmMessageId = llmMessageId;
        this.role = role;
        this.text = text;
        this.createdTimeStamp = createdTimeStamp;
        this.createdDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(createdTimeStamp), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(Initialize.GLOBAL_DATE_FORMAT));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String chatUid;
        private LlmType llmType;
        private String LlmAssistantId;
        private String llmThreadId;
        private String llmRunId;
        private String llmMessageId;
        private String role;
        private String text;
        private Long createdTimeStamp;

        Builder() {
        }

        public Builder withOpenAi(String chatUid, String object, boolean toHtml) {
            this.chatUid = chatUid;
            this.llmType = LlmType.OPENAI;
            this.LlmAssistantId = JsonPath.read(object, "$.assistant_id");
            this.llmThreadId = JsonPath.read(object, "$.thread_id");
            this.llmRunId = JsonPath.read(object, "$.run_id");
            this.llmMessageId = JsonPath.read(object, "$.id");
            this.role = JsonPath.read(object, "$.role");
            if (toHtml) {
                this.text = HelperTools.toHtml(JsonPath.read(object, "$.content[0].text.value"));
            } else {
                this.text = JsonPath.read(object, "$.content[0].text.value");
            }
            this.createdTimeStamp = JsonPath.parse(object).read("$.created_at", Long.class);
            return this;
        }

        public MessagesDTO build() {
            return new MessagesDTO(this.chatUid, this.llmType, this.LlmAssistantId, this.llmThreadId, this.llmRunId, this.llmMessageId, this.role, this.text, this.createdTimeStamp);
        }
    }
}
