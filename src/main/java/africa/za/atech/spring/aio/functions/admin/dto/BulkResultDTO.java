package africa.za.atech.spring.aio.functions.admin.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
public class BulkResultDTO {

    private final String userUid;
    private final String username;
    private final String name;
    private final String surname;
    private final String emailAddress;
    private final String organisationUid;
    private final String moduleUid;
    private final String role;
    private final String assistantsUid;
    private final Boolean created;
    private final List<String> errors;

    private BulkResultDTO(String userUid, String username, String name, String surname, String emailAddress, String organisationUid, String moduleUid, String role, String assistantsUid, Boolean created, List<String> errors) {
        this.userUid = userUid;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.emailAddress = emailAddress;
        this.organisationUid = organisationUid;
        this.moduleUid = moduleUid;
        this.role = role;
        this.assistantsUid = assistantsUid;
        this.created = created;
        this.errors = errors;
    }

    public static Builder builder() {
        return new Builder();
    }

    @ToString
    public static class Builder {
        private String userUid;
        private String username;
        private String name;
        private String surname;
        private String emailAddress;
        private String organisationUid;
        private String moduleUid;
        private String role;
        private String assistantsUid;
        private Boolean created;
        private List<String> errors;

        Builder() {
        }

        public Builder userUid(String userUid) {
            this.userUid = userUid;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder surname(String surname) {
            this.surname = surname;
            return this;
        }

        public Builder emailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public Builder organisationId(String organisationId) {
            this.organisationUid = organisationId;
            return this;
        }

        public Builder moduleUid(String moduleUid) {
            this.moduleUid = moduleUid;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder assistantsUid(String assistantsUid) {
            this.assistantsUid = assistantsUid;
            return this;
        }

        public Builder created(Boolean created) {
            this.created = created;
            return this;
        }

        public Builder errors(List<String> errors) {
            this.errors = errors;
            return this;
        }

        public BulkResultDTO build() {
            return new BulkResultDTO(this.userUid, this.username, this.name, this.surname, this.emailAddress, this.organisationUid, this.moduleUid, this.role, this.assistantsUid, this.created, this.errors);
        }
    }
}
