package act.ring.cncert.restful.repository.auth;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.RepositoryDefinition;

@RepositoryDefinition(domainClass = ForeignSearch.class, idClass = Long.class)
public interface ForeignSearchDao extends PagingAndSortingRepository<ForeignSearch, Long> {

    ForeignSearch findForeignSearchByUserid(long userid);
}