package africa.za.atech.spring.aio.utils;

import africa.za.atech.spring.aio.utils.enums.Result;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OutputTool {

    private Result result;
    private String comment;
    private Object object;

}
