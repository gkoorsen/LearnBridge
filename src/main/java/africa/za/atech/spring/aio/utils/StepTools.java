package africa.za.atech.spring.aio.utils;

import africa.za.atech.spring.aio.functions.assistant.enums.LlmType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StepTools {

    private LlmType llmType;
    private List<Step> steps = new ArrayList<>();

}
