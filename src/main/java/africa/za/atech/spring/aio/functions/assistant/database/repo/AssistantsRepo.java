package africa.za.atech.spring.aio.functions.assistant.database.repo;

import africa.za.atech.spring.aio.functions.assistant.database.model.Assistants;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface AssistantsRepo extends ListCrudRepository<Assistants, Long> {

    List<Assistants> findAllByOrganisationUidAndDisabledIsFalse(String organizationUid);

    List<Assistants> findAllByOrganisationUid(String organizationUid);

    Assistants findByUid(String name);

    Optional<Assistants> findByUidAndDisabledIsFalse(String name);

    Optional<Assistants> findByOrganisationUidAndNameIgnoreCase(String organisationUid, String name);

    Optional<Assistants> findByOrganisationUidAndUid(String organisationUid, String uid);

}
