package africa.za.atech.spring.aio.functions.admin.database.model;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Transactional
@Table(schema = "public", name = "modules")
public class Modules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid")
    private String uid;

    @Column(name = "organisation_uid")
    private String organisationUid;

    @Column(name = "created_datetime")
    private LocalDateTime createdDateTime;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "name")
    private String name;

    @Column(name = "disabled")
    private Boolean disabled;

    @Column(name = "updated_datetime")
    private LocalDateTime updateDatetime;

    @Column(name = "updated_by")
    private String updateBy;

}

