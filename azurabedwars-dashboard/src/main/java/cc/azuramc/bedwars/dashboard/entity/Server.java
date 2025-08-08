package cc.azuramc.bedwars.dashboard.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author An5w1r@163.com
 */
@Data
@Table
@Entity
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Channel ID cannot be blank")
    private String channelId;
    @NotBlank(message = "Display name cannot be blank")
    private String displayName;
    @NotBlank(message = "Default map cannot be blank")
    private String defaultMap;
}
