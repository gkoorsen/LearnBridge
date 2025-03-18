package africa.za.atech.spring.aio.utils.openai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RunThreadMessage {

    @JsonProperty("assistant_id")
    private String assistantId;
    @JsonProperty("additional_instructions")
    private String additionalInstructions;

}
