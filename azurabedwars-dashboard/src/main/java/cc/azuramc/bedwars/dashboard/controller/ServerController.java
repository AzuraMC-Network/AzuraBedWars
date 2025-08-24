package cc.azuramc.bedwars.dashboard.controller;

import cc.azuramc.bedwars.dashboard.entity.ResponseMessage;
import cc.azuramc.bedwars.dashboard.entity.Server;
import cc.azuramc.bedwars.dashboard.entity.dto.ServerDto;
import cc.azuramc.bedwars.dashboard.service.IServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author An5w1r@163.com
 */
@RestController
@RequestMapping("/server")
public class ServerController {

    @Autowired
    IServerService serverService;

    @PostMapping
    public ResponseMessage<Server> addServer(@Validated @RequestBody ServerDto serverDTO) {
        Server server = serverService.addServer(serverDTO);
        return ResponseMessage.success(server);
    }

    @GetMapping
    public ResponseMessage<List<Server>> getAllServers() {
        List<Server> servers = serverService.getAllServers();
        return ResponseMessage.success(servers);
    }

    @GetMapping("/{id}")
    public ResponseMessage<Server> getServerById(@PathVariable Integer id) {
        Optional<Server> server = serverService.getServerById(id);
        return server.map(ResponseMessage::success).orElseGet(() -> ResponseMessage.notFound("Server not found with id: " + id));
    }

    @GetMapping("/name/{displayName}")
    public ResponseMessage<Server> getServerByDisplayName(@PathVariable String displayName) {
        Optional<Server> server = serverService.getServerByDisplayName(displayName);
        return server.map(ResponseMessage::success).orElseGet(() -> ResponseMessage.notFound("Server not found with displayName: " + displayName));
    }

    @PutMapping("/{id}")
    public ResponseMessage<Server> updateServer(@PathVariable Integer id, @Validated @RequestBody ServerDto serverDTO) {
        Optional<Server> updatedServer = serverService.updateServer(id, serverDTO);
        return updatedServer.map(ResponseMessage::success).orElseGet(() -> ResponseMessage.notFound("Server not found with id: " + id));
    }

    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteServer(@PathVariable Integer id) {
        boolean deleted = serverService.deleteServer(id);
        if (deleted) {
            return ResponseMessage.success(null);
        }
        return ResponseMessage.notFound("Server not found with id: " + id);
    }
}
