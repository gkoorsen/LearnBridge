package africa.za.atech.spring.aio.functions.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class NewUserDTO {

    private String organisationUid;
    private String moduleUid;
    private String username;
    private String name;
    private String surname;
    private String emailAddress;
    private String role;
    private List<String> assistantUids;

    public NewUserDTO buildCsvLine(String[] sa) {
        this.username = sa[0];
        this.name = sa[1];
        this.surname = sa[2];
        this.emailAddress = sa[3];
        this.organisationUid = sa[4];
        this.moduleUid = sa[5];
        this.role = sa[6].toUpperCase();
        this.assistantUids = List.of(sa[7]);
        return this;
    }

}
