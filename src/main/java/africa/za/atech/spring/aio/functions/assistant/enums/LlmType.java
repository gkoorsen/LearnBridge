package africa.za.atech.spring.aio.functions.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LlmType {

    OPENAI("OpenAI");

    private final String value;
}
