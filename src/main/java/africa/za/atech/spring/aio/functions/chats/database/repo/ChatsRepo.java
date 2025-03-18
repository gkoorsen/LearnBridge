package africa.za.atech.spring.aio.functions.chats.database.repo;


import africa.za.atech.spring.aio.functions.chats.database.model.Chats;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface ChatsRepo extends ListCrudRepository<Chats, Long> {

    List<Chats> findAllByAssistantsUid(String uid);

    List<Chats> findAllByCreatedBy(String username);

    Chats findByUid(String uid);

    @Transactional
    void deleteAllByCreatedBy(String username);

    @Transactional
    void deleteAllByAssistantsUid(String uid);

}
