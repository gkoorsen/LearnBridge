package africa.za.atech.spring.aio.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AlertType {

    INFO("alert-info"),
    DANGER("alert-danger"),
    SUCCESS("alert-success"),
    WARNING("alert-warning");

    private final String value;
}
