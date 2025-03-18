package africa.za.atech.spring.aio.functions.chats;

import africa.za.atech.spring.aio.functions.admin.dto.UserProfileDTO;
import africa.za.atech.spring.aio.functions.admin.service.UsersService;
import africa.za.atech.spring.aio.functions.assistant.dto.AssistantDTO;
import africa.za.atech.spring.aio.functions.chats.dto.ChatsDTO;
import africa.za.atech.spring.aio.functions.chats.dto.RequestDTO;
import africa.za.atech.spring.aio.utils.Alert;
import africa.za.atech.spring.aio.utils.HelperTools;
import africa.za.atech.spring.aio.utils.OutputTool;
import africa.za.atech.spring.aio.utils.enums.AlertType;
import africa.za.atech.spring.aio.utils.enums.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final UsersService usersService;
    private final ChatService chatService;

    private String showChat(Model model, String assistantUid, RequestDTO requestDTO) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(model, profile);

        if (requestDTO == null) {
            requestDTO = new RequestDTO();
            requestDTO.setUsersName(profile.getName());
            for (AssistantDTO a : profile.getAssistantDTOList()) {
                if (a.getUid().equals(assistantUid)) {
                    requestDTO.setAssistantUid(a.getUid());
                    requestDTO.setAssistantName(a.getName());
                }
            }
        }

        String greeting = HelperTools.getString("static/html/chat-init.html")
                .replace("~NAME~", profile.getName())
                .replace("~ASSISTANT~", requestDTO.getAssistantName());
        model.addAttribute("greeting", greeting);
        model.addAttribute("formObject", requestDTO);
        return "chat/chat";
    }

    @GetMapping("/chat/request")
    public String requestAssistant(RedirectAttributes redirectAttributes) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(redirectAttributes, profile);

        usersService.requestAssistantAssignment(profile);
        redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.WARNING, "No assistants are assigned. A request has been sent to your organisation admin. Please check back later.")));
        return "redirect:/home?requested=true";
    }

    @GetMapping("/chat/continue")
    public String getChatToContinue(Model model,
                                    @RequestParam(value = "uid") String uid) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(model, profile);

        ChatsDTO chatsDTO = chatService.getChat(uid);

        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setChatUid(chatsDTO.getUid());
        requestDTO.setUsersName(profile.getName());
        for (AssistantDTO a : profile.getAssistantDTOList()) {
            if (a.getUid().equals(chatsDTO.getAssistantsUid())) {
                requestDTO.setAssistantUid(a.getUid());
                requestDTO.setAssistantName(a.getName());
            }
        }
        model.addAttribute("chatHistory", chatsDTO.getMessages());
        model.addAttribute("formObject", requestDTO);
        return "chat/chat";
    }

    @GetMapping("/chat")
    public String showChatPage(Model model,
                               @RequestParam(name = "assistant") String assistantUid) {
        return showChat(model, assistantUid, null);
    }

    @GetMapping("/chat/list")
    public String chatManagement(Model model) {
        UserProfileDTO profileDTO = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(model, profileDTO);

        List<ChatsDTO> chatsList = chatService.getUserChats(profileDTO.getUid());
        model.addAttribute("recordList", chatsList);
        return "chat/chat_list";
    }

    @GetMapping("/chat/rename")
    public String getChatDetails(Model model,
                                 @RequestParam(value = "uid") String uid) {
        usersService.addMandatoryAttributes(model);

        ChatsDTO chatsDTO = chatService.getChat(uid);
        model.addAttribute("formObject", chatsDTO);
        return "chat/chat_rename";
    }

    @PostMapping("/chat/rename")
    public String renameChat(RedirectAttributes redirectAttributes,
                             @Validated @ModelAttribute("formObject") ChatsDTO formObject) {
        usersService.addMandatoryAttributes(redirectAttributes);

        OutputTool outputTool = chatService.renameChat(formObject);
        if (outputTool.getResult().equals(Result.EXCEPTION)) {
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.DANGER, outputTool.getComment())));
            return "redirect:/chat/list?updated=false";
        }
        redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, outputTool.getComment())));
        return "redirect:/chat/list";
    }

    @GetMapping("/chat/delete")
    public String deleteChat(RedirectAttributes redirectAttributes,
                             @RequestParam(value = "uid") String uid) {
        usersService.addMandatoryAttributes(redirectAttributes);

        OutputTool outputTool = chatService.deleteChat(uid);
        if (outputTool.getResult().equals(Result.EXCEPTION)) {
            redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.DANGER, outputTool.getComment())));
            return "redirect:/chat/list?deleted=false";
        }
        redirectAttributes.addFlashAttribute("alertList", List.of(new Alert(AlertType.SUCCESS, outputTool.getComment())));
        return "redirect:/chat/list";
    }

    @PostMapping("/chat/process")
    public String processQuestion(Model model, RedirectAttributes redirectAttributes,
                                  @Validated @ModelAttribute("question") RequestDTO requestDTO) {
        UserProfileDTO profile = usersService.getProfile(HelperTools.getLoggedInUsername());
        usersService.addMandatoryAttributes(redirectAttributes, profile);

        String requestReference = profile.getUsername() + "_" + UUID.randomUUID().toString().replaceAll("-", "");
        OutputTool outputTool = chatService.processRequest(profile, requestReference, requestDTO);

        if (!outputTool.getResult().equals((Result.SUCCESS))) {
            if (outputTool.getResult().equals(Result.TIMEOUT)) {
                model.addAttribute("alertList", List.of(new Alert(AlertType.DANGER, "Communication timeout. Please retry.")));
            }
            if (outputTool.getResult().equals((Result.EXCEPTION))) {
                model.addAttribute("alertList", List.of(new Alert(AlertType.DANGER, "Communication error. Please retry.")));
            }
            return showChat(model, requestDTO.getAssistantUid(), (RequestDTO) outputTool.getObject());
        }

        ChatsDTO chatsDTO = (ChatsDTO) outputTool.getObject();
        return "redirect:/chat/continue?uid=" + chatsDTO.getUid();
    }

}
