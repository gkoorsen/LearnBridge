package africa.za.atech.spring.aio.functions.chats.dto;

import lombok.Data;

@Data
public class RequestDTO {

    private String chatUid;
    private String usersName;
    private String assistantUid;
    private String assistantName;
    private String description;
    private String question;

}
