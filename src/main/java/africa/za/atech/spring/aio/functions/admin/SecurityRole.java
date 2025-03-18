package africa.za.atech.spring.aio.functions.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum SecurityRole {

    USER("User"),
    MANAGER("Manager"),
    ORG_ADMIN("Organisation Admin"),
    ADMIN("Administrator");

    private final String value;

    public static SecurityRole fromString(String value) {
        return Arrays.stream(SecurityRole.values())
                .filter(status -> status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}
