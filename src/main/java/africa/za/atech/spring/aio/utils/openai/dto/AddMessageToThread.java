package africa.za.atech.spring.aio.utils.openai.dto;


import africa.za.atech.spring.aio.utils.openai.Role;
import lombok.Getter;

@Getter
public class AddMessageToThread {

    public AddMessageToThread(Role role, String content) {
        this.role = role.getValue();
        this.content = content;
    }

    private final String role;
    private final String content;


}
