package africa.za.atech.spring.aio.functions.admin.database.repo;

import africa.za.atech.spring.aio.functions.admin.database.model.Users;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface UsersRepo extends ListCrudRepository<Users, Long> {

    Optional<Users> findByUsernameIgnoreCase(String username);

    Users findByUid(String uid);

    List<Users> findAllByOrganisationUid(String organisationUid);

    List<Users> findAllByModuleUid(String moduleUid);

    List<Users> findAllByOrganisationUidAndRoleIgnoreCase(String organisationUid, String role);

}
