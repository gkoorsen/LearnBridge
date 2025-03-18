package africa.za.atech.spring.aio.functions.admin.database.model;

import africa.za.atech.spring.aio.functions.admin.SecurityRole;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(schema = "public", name = "users")
@Transactional
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid")
    private String uid;

    @Column(name = "created_datetime")
    private LocalDateTime createdDateTime;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "organisation_uid")
    private String organisationUid;

    @Column(name = "module_uid")
    private String moduleUid;

    @Column(name = "username")
    private String username;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "role")
    private String role;

    @Column(name = "password")
    private String password;

    @Column(name = "mfa_disabled")
    private Boolean mfaDisabled;

    @Nullable
    @Column(name = "mfa_secret")
    private String mfaSecret;

    @Column(name = "assistants_uids")
    private String assistantsUids;

    @Column(name = "disabled")
    private Boolean disabled;

    public Users buildInsert(LocalDateTime createdDateTime,
                             String createdBy,
                             String organisationUid,
                             String moduleUid,
                             String username,
                             String name,
                             String surname,
                             String emailAddress,
                             SecurityRole role,
                             String password,
                             List<String> assistantUidList) {
        this.uid = "u-atech-" + UUID.randomUUID();
        this.createdDateTime = createdDateTime;
        this.createdBy = createdBy;
        this.organisationUid = organisationUid;
        this.moduleUid = moduleUid;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.emailAddress = emailAddress;
        this.role = role.name();
        this.password = password;
        this.mfaDisabled = true;
        this.assistantsUids = assistantUidList.toString();
        this.disabled = false;
        return this;
    }
}

