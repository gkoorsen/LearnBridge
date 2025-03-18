package africa.za.atech.spring.aio.utils;

import africa.za.atech.spring.aio.utils.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Alert {

    private AlertType alertType;
    private String message;

}
