package cc.azuramc.bedwars.dashboard.service;

import cc.azuramc.bedwars.dashboard.entity.Server;
import cc.azuramc.bedwars.dashboard.entity.dto.ServerDto;
import cc.azuramc.bedwars.dashboard.repository.ServerRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author An5w1r@163.com
 */
@Service
public class ServerService implements IServerService {

    @Autowired
    ServerRepository serverRepository;

    @Override
    public Server addServer(ServerDto serverDTO) {
        Server serverEntity = new Server();
        BeanUtils.copyProperties(serverDTO, serverEntity);
        return serverRepository.save(serverEntity);
    }

    @Override
    public List<Server> getAllServers() {
        return (List<Server>) serverRepository.findAll();
    }

    @Override
    public Optional<Server> getServerById(Integer id) {
        return serverRepository.findById(id);
    }

    @Override
    public Optional<Server> updateServer(Integer id, ServerDto serverDTO) {
        Optional<Server> existingServer = serverRepository.findById(id);
        if (existingServer.isPresent()) {
            Server serverEntity = existingServer.get();
            BeanUtils.copyProperties(serverDTO, serverEntity, "id");
            return Optional.of(serverRepository.save(serverEntity));
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteServer(Integer id) {
        if (serverRepository.existsById(id)) {
            serverRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
