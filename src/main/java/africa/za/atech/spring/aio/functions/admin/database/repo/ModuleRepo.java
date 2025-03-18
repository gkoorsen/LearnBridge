package africa.za.atech.spring.aio.functions.admin.database.repo;


import africa.za.atech.spring.aio.functions.admin.database.model.Modules;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface ModuleRepo extends ListCrudRepository<Modules, Long> {

    Modules findByUid(String uid);

    Optional<Modules> findByOrganisationUid(String uid);

    List<Modules> findAllByOrganisationUid(String organisationId);

    Optional<Modules> findByOrganisationUidAndUid(String organisationId, String uid);

    @Transactional
    void deleteAllByUid(String maskedId);
}
