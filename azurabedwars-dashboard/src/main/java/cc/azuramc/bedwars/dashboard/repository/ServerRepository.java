package cc.azuramc.bedwars.dashboard.repository;

import cc.azuramc.bedwars.dashboard.entity.Server;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author An5w1r@163.com
 */
@Repository
public interface ServerRepository extends CrudRepository<Server, Integer> {

    Optional<Server> findByDisplayName(String displayName);

    Optional<Server> findByChannelId(String channelId);
}
