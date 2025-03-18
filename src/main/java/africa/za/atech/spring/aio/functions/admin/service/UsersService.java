package africa.za.atech.spring.aio.functions.admin.service;

import africa.za.atech.spring.aio.config.Initialize;
import africa.za.atech.spring.aio.functions.admin.SecurityRole;
import africa.za.atech.spring.aio.functions.admin.database.model.Modules;
import africa.za.atech.spring.aio.functions.admin.database.model.Organisation;
import africa.za.atech.spring.aio.functions.admin.database.model.Users;
import africa.za.atech.spring.aio.functions.admin.database.repo.ModuleRepo;
import africa.za.atech.spring.aio.functions.admin.database.repo.OrganisationRepo;
import africa.za.atech.spring.aio.functions.admin.database.repo.UsersRepo;
import africa.za.atech.spring.aio.functions.admin.dto.BulkResultDTO;
import africa.za.atech.spring.aio.functions.admin.dto.NewUserDTO;
import africa.za.atech.spring.aio.functions.admin.dto.UserProfileDTO;
import africa.za.atech.spring.aio.functions.admin.enums.SearchLevel;
import africa.za.atech.spring.aio.functions.admin.enums.SearchType;
import africa.za.atech.spring.aio.functions.assistant.AssistantService;
import africa.za.atech.spring.aio.functions.assistant.database.model.Assistants;
import africa.za.atech.spring.aio.functions.assistant.database.repo.AssistantsRepo;
import africa.za.atech.spring.aio.functions.chats.database.repo.ChatsRepo;
import africa.za.atech.spring.aio.utils.EmailTools;
import africa.za.atech.spring.aio.utils.HelperTools;
import africa.za.atech.spring.aio.utils.OutputTool;
import africa.za.atech.spring.aio.utils.enums.Result;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static africa.za.atech.spring.aio.config.Initialize.SYSTEM_ORG_UID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepo usersRepo;
    private final OrganisationRepo orgRepo;
    private final ModuleRepo modRepo;
    private final ChatsRepo chatsRepo;
    private final ModuleRepo moduleRepo;
    private final AssistantsRepo assistantsRepo;

    @Value("${atech.app.email.from.email-address}")
    private String fromEmailAddress;
    @Value("${atech.app.name}")
    private String appName;
    @Value("${atech.app.url.login}")
    private String appUrl;

    private final UsersRepo repoUsers;
    private final EmailTools emailTools;

    private final AssistantService assistantService;

    private final PasswordEncoder encoder;

    public OutputTool createNewUser(UserProfileDTO requestingUser, NewUserDTO dto) {
        String password = HelperTools.generatePassword(9, 2, 0);

        UserProfileDTO newUser = addUser(LocalDateTime.now(), requestingUser.getUsername(), dto.getOrganisationUid(), dto.getModuleUid(),
                dto.getUsername(), dto.getName(), dto.getSurname(), dto.getEmailAddress(), SecurityRole.valueOf(dto.getRole()),
                password, dto.getAssistantUids());

        String subject = "Registration Successful: Welcome to ~APP_NAME~!"
                .replaceAll("~APP_NAME~", appName);
        String body = HelperTools.getString("static/html/email-add_user.html");
        body = body.replaceAll("~USER~", newUser.getName() + " " + newUser.getSurname());
        body = body.replaceAll("~USERNAME~", newUser.getUsername());
        body = body.replaceAll("~APP_NAME~", appName);
        body = body.replaceAll("~TEMP_PASSWORD~", password);
        body = body.replaceAll("~APP_URL~", appUrl);
        body = body.replaceAll("~FROM_EMAIL_ADDRESS~", fromEmailAddress);

        OutputTool outputTool = emailTools.send(List.of(newUser.getEmailAddress()), subject, body, null, List.of(fromEmailAddress), null);
        if (outputTool.getResult().equals(Result.EXCEPTION)) {
            return outputTool;
        } else {
            return new OutputTool(Result.SUCCESS, "Registration completed successfully. Please check your email for additional information.", newUser);
        }
    }

    public List<UserProfileDTO> getProfiles(UserProfileDTO profile) {
        if (profile.getOrganisationUid().equals(SYSTEM_ORG_UID)) {
            return repoUsers.findAll().stream()
                    .map(users -> getProfile(SearchType.USERNAME, users.getUsername())).collect(Collectors.toList());
        } else {
            return repoUsers.findAllByOrganisationUid(profile.getOrganisationUid()).stream()
                    .map(users -> getProfile(SearchType.USERNAME, users.getUsername())).collect(Collectors.toList());
        }
    }

    public List<UserProfileDTO> getProfiles(SearchLevel searchLevel, String searchUid) {
        return switch (searchLevel) {
            case ORGANISATION ->
                    usersRepo.findAllByOrganisationUid(searchUid).stream().map(users -> getProfile(SearchType.USERNAME, users.getUsername())).toList();
            case MODULE ->
                    usersRepo.findAllByModuleUid(searchUid).stream().map(users -> getProfile(SearchType.USERNAME, users.getUsername())).toList();
        };
    }

    public UserProfileDTO getProfile(Users user) {
        return new UserProfileDTO().build(
                orgRepo.findByUid(user.getOrganisationUid()),
                modRepo.findByUid(user.getModuleUid()),
                user,
                assistantService.getAllUserAssistants(user.getAssistantsUids()));
    }

    public UserProfileDTO getProfile(String username) {
        return getProfile(SearchType.USERNAME, username);
    }

    public UserProfileDTO getProfile(SearchType type, String searchTerm) {
        if (type.equals(SearchType.UID)) {
            return getProfile(repoUsers.findByUid(searchTerm));
        } else {
            Optional<Users> user = repoUsers.findByUsernameIgnoreCase(searchTerm);
            return user.map(this::getProfile).orElse(null);
        }
    }

    public UserProfileDTO getProfileFromUid(String uid) {
        return getProfile(repoUsers.findByUid(uid));
    }

    public OutputTool updateProfile(UserProfileDTO form, List<String> assistantIds) {
        OutputTool out = new OutputTool(Result.SUCCESS, "", null);
        boolean isPasswordUpdate = form.getNewPassword() != null;

        Users userRecord = repoUsers.findByUid(form.getUid());

        if (isPasswordUpdate) {
            if (!form.getNewPassword().isEmpty() && !form.getConfirmPassword().isEmpty()) {
                userRecord.setPassword(encoder.encode(form.getNewPassword()));
                out.setComment("Password changed successfully.");
            }
        } else {
            if (form.getUsername() != null) {
                userRecord.setUsername(form.getUsername());
            }
            if (form.getOrganisationUid() != null && !form.getOrganisationUid().equals(userRecord.getOrganisationUid())) {
                userRecord.setOrganisationUid(form.getOrganisationUid());
            }
            if (form.getModuleUid() != null) {
                userRecord.setModuleUid(form.getModuleUid());
            }
            if (!userRecord.getName().equalsIgnoreCase(form.getName())) {
                userRecord.setName(form.getName());
            }
            if (!userRecord.getSurname().equalsIgnoreCase(form.getSurname())) {
                userRecord.setSurname(form.getSurname());
            }
            if (!userRecord.getEmailAddress().equalsIgnoreCase(form.getEmailAddress())) {
                userRecord.setEmailAddress(form.getEmailAddress());
            }
            if (assistantIds != null) {
                userRecord.setAssistantsUids(assistantIds.toString());
            } else {
                if (form.getOrganisationUid() != null && form.getModuleUid() != null) {
                    userRecord.setAssistantsUids(new ArrayList<String>().toString());
                }
            }
            if (form.getRoles() != null) {
                userRecord.setRole(form.getRoles().toString());
            }
            if (form.getDisabled() != null) {
                userRecord.setDisabled(form.getDisabled());
            }

            out.setComment("Profile updated successfully.");
        }

        repoUsers.save(userRecord);
        out.setResult(Result.SUCCESS);
        return out;
    }

    public OutputTool forgotPassword(String username) {
        Optional<Users> lookupRecord = repoUsers.findByUsernameIgnoreCase(username);
        if (lookupRecord.isEmpty()) {
            return new OutputTool(Result.EXCEPTION, "Unable to reset password. Account not found.", null);
        }

        if (lookupRecord.get().getDisabled()) {
            return new OutputTool(Result.EXCEPTION, "Unable to reset password. Account has been disabled.", null);
        }
        String password = HelperTools.generatePassword(9, 2, 0);

        lookupRecord.get().setPassword(encoder.encode(password));
        repoUsers.save(lookupRecord.get());

        String subject = "Temporary Credentials";
        String body = HelperTools.getString("static/html/email-forgot_password.html");
        body = body.replaceAll("~USER~", lookupRecord.get().getName() + " " + lookupRecord.get().getSurname());
        body = body.replaceAll("~APP_NAME~", appName);
        body = body.replaceAll("~TEMP_PASSWORD~", password);
        body = body.replaceAll("~APP_URL~", appUrl);
        body = body.replaceAll("~FROM_EMAIL_ADDRESS~", fromEmailAddress);

        OutputTool outputTool = emailTools.send(List.of(lookupRecord.get().getEmailAddress()), subject, body, null, null, null);
        if (!outputTool.getResult().equals(Result.SUCCESS)) {
            return outputTool;
        }
        return new OutputTool(Result.SUCCESS, "Temp password send to registered email", null);
    }

    public OutputTool deleteProfile(String uid) {
        Users user = repoUsers.findByUid(uid);
        if (user.getCreatedBy().equalsIgnoreCase(Initialize.SYSTEM_ORG_NAME)) {
            return new OutputTool(Result.EXCEPTION, "Unable to process request", null);
        }
        chatsRepo.deleteAllByCreatedBy(user.getUid());
        usersRepo.delete(user);
        return new OutputTool(Result.SUCCESS, "User deleted successfully.", null);
    }

    public Model addMandatoryAttributes(Model model) {
        UserProfileDTO profile = getProfile(SearchType.USERNAME, HelperTools.getLoggedInUsername());
        return addMandatoryAttributes(model, profile);
    }

    public Model addMandatoryAttributes(Model model, UserProfileDTO profile) {
        model.addAttribute("profileObject", profile);
        model.addAttribute("app_name", appName);
        return model;
    }

    public RedirectAttributes addMandatoryAttributes(RedirectAttributes redirectAttributes) {
        UserProfileDTO profile = getProfile(SearchType.USERNAME, HelperTools.getLoggedInUsername());
        return addMandatoryAttributes(redirectAttributes, profile);
    }

    public RedirectAttributes addMandatoryAttributes(RedirectAttributes redirectAttributes, UserProfileDTO profile) {
        redirectAttributes.addFlashAttribute("profileObject", profile);
        redirectAttributes.addFlashAttribute("app_name", appName);
        return redirectAttributes;
    }

    private UserProfileDTO addUser(LocalDateTime createdDateTime,
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
        Users userRecord = new Users().buildInsert(
                createdDateTime,
                createdBy,
                organisationUid,
                moduleUid,
                username,
                name,
                surname,
                emailAddress,
                role,
                encoder.encode(password),
                assistantUidList);
        if (organisationUid.equalsIgnoreCase(SYSTEM_ORG_UID)) {
            userRecord.setUid(userRecord.getUid().substring(0, 16));
        }
        repoUsers.save(userRecord);
        return getProfile(SearchType.USERNAME, userRecord.getUsername());
    }

    @Async
    @SneakyThrows
    public void processCsv(UserProfileDTO requestingUser, MultipartFile csvFile) {
        String tempFile = System.getProperty("java.io.tmpdir") + "/" + csvFile.getOriginalFilename();
        List<String[]> csvData = HelperTools.readCsv(csvFile, System.getProperty("java.io.tmpdir"));
        csvData.remove(0);
        List<NewUserDTO> newUserDTOList = new ArrayList<>(csvData.size());
        for (String[] sa : csvData) {
            newUserDTOList.add(new NewUserDTO().buildCsvLine(sa));
        }
        FileUtils.delete(new File(tempFile));

        List<String> errors;
        List<SecurityRole> securityRoles = new ArrayList<>(List.of(SecurityRole.values()));
        securityRoles.removeIf(sec -> sec.equals(SecurityRole.ADMIN));

        List<BulkResultDTO> results = new ArrayList<>(newUserDTOList.size());
        List<String[]> outputCsv = new ArrayList<>();

        for (NewUserDTO item : newUserDTOList) {
            BulkResultDTO.Builder builder = BulkResultDTO.builder();
            errors = new ArrayList<>();

            builder.name(item.getName());
            builder.surname(item.getSurname());
            builder.emailAddress(item.getEmailAddress());

            Optional<Users> lookupUsername = usersRepo.findByUsernameIgnoreCase(item.getUsername());
            if (lookupUsername.isEmpty()) {
                builder.username(item.getUsername());
            } else {
                builder.username(item.getUsername());
                errors.add("username already exists");
            }

            SecurityRole role = (SecurityRole.fromString(item.getRole()));
            if (role != null) {
                if (securityRoles.contains(role)) {
                    builder.role(role.name());
                } else {
                    builder.role(item.getRole());
                    errors.add("invalid role");
                }
            } else {
                builder.role(item.getRole());
                errors.add("invalid role");
            }

            Organisation organisation = orgRepo.findByUid(item.getOrganisationUid());
            if (organisation != null) {
                builder.organisationId(organisation.getUid());
            } else {
                builder.organisationId(item.getOrganisationUid());
                errors.add("invalid organisation");
            }

            if (organisation != null) {
                Optional<Modules> moduleUid = moduleRepo.findByOrganisationUidAndUid(organisation.getUid(), item.getModuleUid());
                if (moduleUid.isPresent()) {
                    builder.moduleUid(moduleUid.get().getUid());
                } else {
                    builder.moduleUid(item.getModuleUid());
                    errors.add("invalid module");
                }
            } else {
                builder.moduleUid(item.getModuleUid());
                errors.add("invalid org for module lookup");
            }

            if (organisation != null) {
                Optional<Assistants> assistants = assistantsRepo.findByOrganisationUidAndUid(organisation.getUid(), item.getAssistantUids().get(0));
                if (assistants.isPresent()) {
                    builder.assistantsUid(assistants.get().getUid());
                } else {
                    builder.assistantsUid(item.getAssistantUids().get(0));
                    errors.add("invalid assistant");
                }
            } else {
                builder.assistantsUid(item.getAssistantUids().get(0));
                errors.add("invalid org for assistant lookup");
            }

            if (errors.isEmpty()) {
                OutputTool out = createNewUser(requestingUser, item);
                if (out.getResult().equals(Result.SUCCESS)) {
                    UserProfileDTO profile = (UserProfileDTO) out.getObject();
                    builder.userUid(profile.getUid());
                    builder.created(true);
                    errors.add("-");
                } else {
                    builder.userUid("-");
                    builder.created(false);
                    errors.add(out.getComment());
                }
            } else {
                builder.userUid("-");
                builder.created(false);
            }
            builder.errors(errors);
            results.add(builder.build());
        }

        outputCsv.add(new String[]{
                "username", "name", "surname", "email_address",
                "organisation_uid", "module_uid", "role", "assistant_uid",
                "created", "userId", "errors"});
        outputCsv.addAll(results.stream()
                .map(obj -> new String[]{
                        obj.getUsername(), obj.getName(), obj.getSurname(), obj.getEmailAddress(),
                        obj.getOrganisationUid(), obj.getModuleUid(), obj.getRole(), obj.getAssistantsUid(),
                        String.valueOf(obj.getCreated()), obj.getUserUid(), obj.getErrors().toString()})
                .toList());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        CSVWriter csvWriter = new CSVWriter(writer);
        csvWriter.writeAll(outputCsv);
        writer.flush();

        byte[] data = outputStream.toByteArray();
        MultipartFile file = new MockMultipartFile(
                "bulk-result.csv",
                "bulk-result.csv",
                "text/csv",
                data);

        emailTools.send(List.of(requestingUser.getEmailAddress()), "Bulk registration result", "", null, null, List.of(file));
        log.info("Bulk process completed successfully");
    }

    @Async
    @SneakyThrows
    public void requestAssistantAssignment(UserProfileDTO userProfileDTO) {
        List<String> orgAdminList = repoUsers.findAllByOrganisationUidAndRoleIgnoreCase(userProfileDTO.getOrganisationUid(), SecurityRole.ORG_ADMIN.name())
                .stream()
                .map(Users::getEmailAddress)
                .toList();
        if (!orgAdminList.isEmpty()) {
            String subject = "User Assistant Assignment: " + userProfileDTO.getUsername();
            String body = HelperTools.getString("static/html/email-user_assistant_request.html");
            body = body.replaceAll("~USER~", userProfileDTO.getName() + " " + userProfileDTO.getSurname());
            body = body.replaceAll("~USERNAME~", userProfileDTO.getUsername());
            body = body.replaceAll("~ORG_NAME~", userProfileDTO.getOrganisationName());
            body = body.replaceAll("~MOD_NAME~", userProfileDTO.getModuleName());
            emailTools.send(orgAdminList, subject, body, null, List.of(fromEmailAddress), null);
        }
    }
}

