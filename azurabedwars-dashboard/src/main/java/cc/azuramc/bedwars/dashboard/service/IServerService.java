package cc.azuramc.bedwars.dashboard.service;

import cc.azuramc.bedwars.dashboard.entity.Server;
import cc.azuramc.bedwars.dashboard.entity.dto.ServerDto;

import java.util.List;
import java.util.Optional;

/**
 * @author An5w1r@163.com
 */
public interface IServerService {

    Server addServer(ServerDto serverDTO);

    List<Server> getAllServers();

    Optional<Server> getServerById(Integer id);

    Optional<Server> updateServer(Integer id, ServerDto serverDTO);

    boolean deleteServer(Integer id);
}
