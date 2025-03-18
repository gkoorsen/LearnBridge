package africa.za.atech.spring.aio.functions.chats;

import africa.za.atech.spring.aio.functions.admin.dto.UserProfileDTO;
import africa.za.atech.spring.aio.functions.assistant.database.model.Assistants;
import africa.za.atech.spring.aio.functions.assistant.database.repo.AssistantsRepo;
import africa.za.atech.spring.aio.functions.assistant.enums.LlmType;
import africa.za.atech.spring.aio.functions.chats.database.model.Chats;
import africa.za.atech.spring.aio.functions.chats.database.repo.ChatsRepo;
import africa.za.atech.spring.aio.functions.chats.dto.ChatsDTO;
import africa.za.atech.spring.aio.functions.chats.dto.RequestDTO;
import africa.za.atech.spring.aio.utils.OutputTool;
import africa.za.atech.spring.aio.utils.Step;
import africa.za.atech.spring.aio.utils.StepTools;
import africa.za.atech.spring.aio.utils.enums.Result;
import africa.za.atech.spring.aio.utils.openai.OpenAiClient;
import africa.za.atech.spring.aio.utils.openai.Role;
import com.jayway.jsonpath.JsonPath;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static africa.za.atech.spring.aio.utils.HelperTools.wrapVar;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${atech.app.openai.api.assistant.response.poll-max-retry}")
    private int pollMaxRetry;
    @Value("${atech.app.openai.api.assistant.response.poll-sleep-duration}")
    private int pollSleepTime;

    private final AssistantsRepo repoAssistants;
    private final ChatsRepo repoChats;

    public List<ChatsDTO> getUserChats(String usersUid) {
        return repoChats.findAllByCreatedBy(usersUid).stream()
                .map(c -> ChatsDTO.builder()
                        .withOpenAi(repoAssistants.findByUid(c.getAssistantsUid()), c)
                        .build())
                .toList();
    }

    public ChatsDTO getChat(String uid) {
        Chats chats = repoChats.findByUid(uid);
        Assistants assistant = repoAssistants.findByUid(chats.getAssistantsUid());
        return ChatsDTO.builder().withOpenAi(assistant, chats).build();
    }

    public OutputTool renameChat(ChatsDTO chatsDTO) {
        Chats rec = repoChats.findByUid(chatsDTO.getUid());
        rec.setDescription(chatsDTO.getDescription().trim());
        repoChats.save(rec);
        return new OutputTool(Result.SUCCESS, "Chat renamed successfully", null);
    }

    public OutputTool deleteChat(String uid) {
        Chats rec = repoChats.findByUid(uid);
        repoChats.delete(rec);
        return new OutputTool(Result.SUCCESS, "Chat deleted successfully.", null);
    }

    public OutputTool processRequest(UserProfileDTO userprofileDTO, String requestId, RequestDTO requestDTO) {
        boolean isNewChat = requestDTO.getChatUid() == null || requestDTO.getChatUid().isBlank();
        Assistants assistant = repoAssistants.findByUid(requestDTO.getAssistantUid());
        if (assistant.getLlmType().equalsIgnoreCase(LlmType.OPENAI.name())) {
            return withOpenAi(userprofileDTO, requestId, requestDTO, assistant, isNewChat);
        } else {
            return new OutputTool(Result.SUCCESS, "", null);
        }
    }

    public OutputTool withOpenAi(UserProfileDTO userprofileDTO, String requestId, RequestDTO requestDTO, Assistants assistant, Boolean isNewChat) {

        StepTools stepTools = new StepTools();
        stepTools.setLlmType(LlmType.OPENAI);

        String THREAD_ID;
        String RUN_ID;

        if (isNewChat) {
            log.info("{} - STEP 1 [NEW]: Create thread with openai", requestId);
            Response step1Response = OpenAiClient.createThread(requestId, assistant);
            if (step1Response.getStatusCode() != 200) {
                return new OutputTool(Result.EXCEPTION, "Unable to create a new thread", null);
            }
            THREAD_ID = JsonPath.read(step1Response.getBody().asString(), "$.id");
            stepTools.getSteps().add(Step.builder().with(1, "create thread", String.valueOf(step1Response.getStatusCode()), step1Response.getBody().asString()).build());
        } else {
            Chats existingChatLookup = repoChats.findByUid(requestDTO.getChatUid());
            THREAD_ID = existingChatLookup.getLlmThreadId();
            requestId = existingChatLookup.getUid();
            log.info("{} - STEP 1 [EXISTING]: Existing openai thread identified", requestId);
        }

        log.info("{} - STEP 2: Create message using thread_id: {}", requestId, THREAD_ID);
        Response step2Response = OpenAiClient.addMessage(requestId, THREAD_ID, assistant, Role.USER, requestDTO.getQuestion());
        if (step2Response.getStatusCode() != 200) {
            return new OutputTool(Result.EXCEPTION, "Unable to create a message using thread_id: " + wrapVar(THREAD_ID), null);
        }
        stepTools.getSteps().add(Step.builder().with(2, "create message", String.valueOf(step2Response.getStatusCode()), step2Response.getBody().asString()).build());

        log.info("{} - STEP 3: Post run using the thread_id: {}", requestId, THREAD_ID);
        Response step3Response = OpenAiClient.runThread(requestId, THREAD_ID, assistant, "");
        if (step3Response.getStatusCode() != 200) {
            return new OutputTool(Result.EXCEPTION, "Unable to create a run using thread_id: " + wrapVar(THREAD_ID), null);
        }
        RUN_ID = JsonPath.read(step3Response.getBody().asString(), "$.id");
        stepTools.getSteps().add(Step.builder().with(3, "run thread", String.valueOf(step3Response.getStatusCode()), step3Response.getBody().asString()).build());

        log.info("{} - STEP 4: Polling for completion", requestId);
        try {
            CompletableFuture<Response> pollingResponse = withOpenAiPolling(requestId, THREAD_ID, RUN_ID, assistant, Executors.newSingleThreadScheduledExecutor());
            Response step4Response = pollingResponse.join(); // This will block until the polling completes
            stepTools.getSteps().add(Step.builder().with(4, "poll status", String.valueOf(step4Response.getStatusCode()), step4Response.getBody().asString()).build());
        } catch (RuntimeException e) {
            return new OutputTool(Result.EXCEPTION, "Polling failed with message: " + e.getMessage(), requestDTO);
        }

        log.info("{} - STEP 5: Getting messages and answer to populate to the response object", requestId);
        Response finalResponse = OpenAiClient.getMessages(requestId, THREAD_ID, assistant);
        stepTools.getSteps().add(Step.builder().with(5, "final response", String.valueOf(finalResponse.getStatusCode()), finalResponse.getBody().asString()).build());

        Chats chatRecord = persistChat(userprofileDTO, isNewChat, THREAD_ID, requestDTO, finalResponse, assistant, stepTools);
        return new OutputTool(Result.SUCCESS, "", ChatsDTO.builder().withOpenAi(assistant, chatRecord).build());
    }

    public CompletableFuture<Response> withOpenAiPolling(String requestId, String threadId, String runId, Assistants assistant, ScheduledExecutorService scheduler) {
        AtomicReference<String> POLLING_STATUS = new AtomicReference<>("pending");
        final String requestRef = requestId; // Ensure it's effectively final
        AtomicInteger iterations = new AtomicInteger(0);
        CompletableFuture<Response> future = new CompletableFuture<>();

        scheduler.scheduleAtFixedRate(() -> {
            String currentStatus = POLLING_STATUS.get();
            log.debug("{} - STEP 4: Polling [{}/{}] with last status: {}", requestRef, iterations.get(), pollMaxRetry, currentStatus.toUpperCase());
            if ("completed".equals(currentStatus)) {
                scheduler.shutdown();
                future.complete(OpenAiClient.getRunStatus(requestRef, threadId, runId, assistant));
            } else if (iterations.incrementAndGet() > pollMaxRetry) {
                log.error("{} - STEP 4: Max polling retries reached [{}/{}] with last status: {}", requestRef, iterations.get(), pollMaxRetry, currentStatus.toUpperCase());
                scheduler.shutdown();
                future.completeExceptionally(new RuntimeException("Max polling retries reached"));
            } else {
                Response step4PollingResponse = OpenAiClient.getRunStatus(requestRef, threadId, runId, assistant);
                if (step4PollingResponse.getStatusCode() != 200) {
                    log.error("{} - STEP 4: Polling failed for thread: {} and run id: {}", requestRef, threadId, runId);
                    scheduler.shutdown();
                    future.completeExceptionally(new RuntimeException("Polling failed"));
                    return;
                }
                POLLING_STATUS.set(JsonPath.read(step4PollingResponse.getBody().asString(), "$.status"));
                log.debug("{} - STEP 4: Polling [{}/{}] with current status: {}", requestRef, iterations.get(), pollMaxRetry, POLLING_STATUS.get().toUpperCase());
            }
        }, 0, pollSleepTime, TimeUnit.MILLISECONDS);
        return future;
    }

    @SuppressWarnings("all")
    public Chats persistChat(UserProfileDTO userprofileDTO, Boolean isNewChat, String threadId, RequestDTO requestDTO, Response response, Assistants assistant, StepTools stepTools) {
        String answer = JsonPath.read(response.getBody().asString(), "$.data[0].content[0].text.value");
        String lastMessageId = JsonPath.read(response.getBody().asString(), "$.last_id");

        Chats chatRecord;
        if (isNewChat) {
            String firstMessageId = JsonPath.read(response.getBody().asString(), "$.first_id");
            chatRecord = new Chats().buildInsert(LocalDateTime.now(),
                    assistant.getUid(),
                    requestDTO.getDescription(),
                    assistant.getLlmAssistantId(),
                    threadId, firstMessageId, lastMessageId, response.getBody().asString());
            chatRecord.setUid("c-" + UUID.randomUUID());
            chatRecord.setAssistantsUid(assistant.getUid());
            chatRecord.setCreatedDateTime(LocalDateTime.now());
            chatRecord.setCreatedBy(userprofileDTO.getUid());
            if (requestDTO.getQuestion().length() > 21) {
                chatRecord.setDescription(requestDTO.getQuestion().substring(0, 20).trim() + " ...");
            } else {
                chatRecord.setDescription(requestDTO.getQuestion().trim() + " ...");
            }
            chatRecord.setLlmAssistantId(assistant.getLlmAssistantId());
            chatRecord.setLlmThreadId(threadId);
            chatRecord.setLlmFirstMessageId(JsonPath.read(response.getBody().asString(), "$.first_id"));
            chatRecord.setLlmLastMessageId(lastMessageId);
            chatRecord.setLlmResponseObject(response.getBody().asString());
        } else {
            chatRecord = repoChats.findByUid(requestDTO.getChatUid());
        }
        chatRecord.setUpdatedDatetime(LocalDateTime.now());
        chatRecord.setLlmLastMessageId(lastMessageId);
        chatRecord.setLlmResponseObject(response.getBody().asString());
        return repoChats.save(chatRecord);
    }

}
