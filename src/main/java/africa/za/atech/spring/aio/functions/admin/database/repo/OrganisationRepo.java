package africa.za.atech.spring.aio.functions.admin.database.repo;

import africa.za.atech.spring.aio.functions.admin.database.model.Organisation;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface OrganisationRepo extends ListCrudRepository<Organisation, Long> {

    Organisation findByUid(String uid);

    Optional<Organisation> findByNameIgnoreCase(String name);
}
