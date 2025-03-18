package africa.za.atech.spring.aio.functions.admin.dto;

import africa.za.atech.spring.aio.functions.admin.database.model.Modules;
import africa.za.atech.spring.aio.functions.admin.database.model.Organisation;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ModuleDTO {

    private String uid;
    private LocalDateTime createdDateTime;
    private String createdBy;
    private String organisationUid;
    private String organisationName;
    private String name;
    private Boolean disabled;
    private LocalDateTime updateDatetime;
    private String updateBy;
    private List<UserProfileDTO> usersList;
    private Integer userCount;
    private boolean delete;

    public ModuleDTO build(Organisation organisation, Modules modules, List<UserProfileDTO> usersList) {
        this.uid = modules.getUid();
        this.organisationUid = organisation.getUid();
        this.organisationName = organisation.getName();
        this.name = modules.getName();
        this.createdDateTime = modules.getCreatedDateTime();
        this.createdBy = modules.getCreatedBy();
        this.disabled = modules.getDisabled();
        this.updateDatetime = modules.getUpdateDatetime();
        this.updateBy = modules.getUpdateBy();
        this.usersList = usersList;
        this.userCount = usersList.size();
        if (this.userCount == 0) {
            this.delete = true;
        }
        return this;
    }

}

