package act.ring.cncert.restful.repository.auth;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.RepositoryDefinition;

@RepositoryDefinition(domainClass = User.class, idClass = Long.class)
public interface UserDao extends PagingAndSortingRepository<User, Long> {

    User findFirstByLoginName(String loginName);

    User findFirstByEmail(String email);
}
