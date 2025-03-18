package africa.za.atech.spring.aio.config;

import africa.za.atech.spring.aio.functions.admin.SecurityRole;
import africa.za.atech.spring.aio.functions.admin.database.model.Modules;
import africa.za.atech.spring.aio.functions.admin.database.model.Organisation;
import africa.za.atech.spring.aio.functions.admin.database.model.Users;
import africa.za.atech.spring.aio.functions.admin.database.repo.ModuleRepo;
import africa.za.atech.spring.aio.functions.admin.database.repo.OrganisationRepo;
import africa.za.atech.spring.aio.functions.admin.database.repo.UsersRepo;
import africa.za.atech.spring.aio.utils.openai.OpenAiClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class Initialize {

    @Value("${atech.app.config.user.admin.username}")
    private String systemAdminUsername;
    @Value("${atech.app.config.user.admin.create}")
    private boolean createSystemAdminUser;
    @Value("${atech.app.config.user.admin.name}")
    private String systemAdminName;
    @Value("${atech.app.config.user.admin.surname}")
    private String systemAdminSurname;
    @Value("${atech.app.config.user.admin.email}")
    private String systemAdminEmailAddress;
    @Value("${atech.app.config.user.admin.pwd}")
    private String systemAdminPwd;

    @Value("${atech.app.openai.api.base-url}")
    private String openAiBaseApiUrl;
    @Value("${atech.app.openai.api.user-relaxed-https}")
    private boolean useRelaxedHttps;
    @Value("${atech.app.openai.api.assistant.beta-version}")
    private int openApiAssistantsBetaVersion;

    @Value("${atech.app.openai.api.assistant.logout.create-thread}")
    private boolean logoutCreateThread;
    @Value("${atech.app.openai.api.assistant.logout.add-message}")
    private boolean logoutAddMessage;
    @Value("${atech.app.openai.api.assistant.logout.run-thread}")
    private boolean logoutRunThread;
    @Value("${atech.app.openai.api.assistant.logout.get-run-status}")
    private boolean logoutGetRunStatus;
    @Value("${atech.app.openai.api.assistant.logout.get-messages}")
    private boolean logoutGetMessages;
    @Value("${atech.app.openai.api.assistant.logout.get-steps-for-run}")
    private boolean logoutGetStepsForRun;

    private final PasswordEncoder encoder;

    private final OrganisationRepo repoOrganisation;
    private final ModuleRepo repoModule;
    private final UsersRepo repoUsers;

    public static String SYSTEM_ORG_UID;
    public static String SYSTEM_MOD_UID;
    public static final String SYSTEM_ORG_NAME = "System";
    public static final String SYSTEM_MOD_NAME = "Administrators";
    public static String GLOBAL_DATE_FORMAT;

    @PostConstruct
    public void initializeSystem() {

        GLOBAL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

        Optional<Organisation> orgLookupRecord = repoOrganisation.findByNameIgnoreCase(SYSTEM_ORG_NAME);
        Organisation orgRecord;
        if (orgLookupRecord.isEmpty()) {
            orgRecord = new Organisation();
            orgRecord.setUid(("o-atech-" + UUID.randomUUID()).substring(0, 15));
            orgRecord.setCreatedBy(SYSTEM_ORG_NAME.toLowerCase());
            orgRecord.setCreatedDateTime(LocalDateTime.now());
            orgRecord.setName(SYSTEM_ORG_NAME);
            orgRecord.setDisabled(false);
            repoOrganisation.save(orgRecord);
            log.info("*** System Organisation has been created with uid: {}", orgRecord.getUid());
        } else {
            orgRecord = orgLookupRecord.get();
        }
        SYSTEM_ORG_UID = orgRecord.getUid();

        Optional<Modules> moduleLookup = repoModule.findByOrganisationUid(orgRecord.getUid());
        Modules moduleRecord;
        if (moduleLookup.isEmpty()) {
            moduleRecord = new Modules();
            moduleRecord.setUid(("m-atech-" + UUID.randomUUID()).substring(0, 15));
            moduleRecord.setOrganisationUid(orgRecord.getUid());
            moduleRecord.setCreatedDateTime(LocalDateTime.now());
            moduleRecord.setCreatedBy(SYSTEM_ORG_NAME.toLowerCase());
            moduleRecord.setName(SYSTEM_MOD_NAME);
            moduleRecord.setDisabled(false);
            repoModule.save(moduleRecord);
            log.info("*** System Module has been created with uid: {}", moduleRecord.getUid());
        } else {
            moduleRecord = moduleLookup.get();
        }
        SYSTEM_MOD_UID = moduleRecord.getUid();

        Optional<Users> record = repoUsers.findByUsernameIgnoreCase(systemAdminUsername);
        Users userRecord;
        if (record.isEmpty()) {
            if (createSystemAdminUser) {
                userRecord = new Users().buildInsert(
                        LocalDateTime.now(), SYSTEM_ORG_NAME.toLowerCase(),
                        orgRecord.getUid(),
                        moduleRecord.getUid(),
                        systemAdminUsername, systemAdminName, systemAdminSurname, systemAdminEmailAddress, SecurityRole.ADMIN,
                        encoder.encode(systemAdminPwd), new ArrayList<>());
                userRecord.setUid(userRecord.getUid().substring(0, 16));
                repoUsers.save(userRecord);
                log.info("*** System ADMIN has been created with uid: {}", userRecord.getUid());
            }
        }
    }

    @PostConstruct
    public void setOpenAiStatics() {
        if (openAiBaseApiUrl.endsWith("/")) {
            openAiBaseApiUrl = openAiBaseApiUrl.substring(0, openAiBaseApiUrl.length() - 1);
        }
        if (openApiAssistantsBetaVersion != 2) {
            throw new RuntimeException("OpenAPI assistants beta version must be 2");
        }
        OpenAiClient.openAiBaseApiUrl = openAiBaseApiUrl;
        OpenAiClient.openApiAssistantsBetaVersion = openApiAssistantsBetaVersion;
        OpenAiClient.useRelaxedHttps = useRelaxedHttps;

        OpenAiClient.logoutCreateThread = logoutCreateThread;
        OpenAiClient.logoutAddMessage = logoutAddMessage;
        OpenAiClient.logoutRunThread = logoutRunThread;
        OpenAiClient.logoutGetRunStatus = logoutGetRunStatus;
        OpenAiClient.logoutGetMessages = logoutGetMessages;
        OpenAiClient.logoutGetStepsForRun = logoutGetStepsForRun;
    }
}
