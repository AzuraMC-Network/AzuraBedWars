package cc.azuramc.bedwars.jedis.util;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * IP工具类
 * 用于获取本地IP地址和端口
 *
 * @author an5w1r@163.com
 */
public class IPUtil {

    /**
     * 获取本地IPv4地址和服务器端口
     * @return 格式为 "IP:端口" 的字符串,如果获取失败则返回null
     */
    @SneakyThrows
    public static String getLocalIp() {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (isValidIPv4(address.getHostAddress())) {
                    return formatIpAndPort(address.getHostAddress(), Bukkit.getPort());
                }
            }
        }
        
        return null;
    }

    /**
     * 检查是否为有效的IPv4地址
     * @param ip IP地址
     * @return 如果是有效的IPv4地址返回true
     */
    private static boolean isValidIPv4(String ip) {
        return ip.split("\\.").length == 4;
    }

    /**
     * 格式化IP地址和端口
     * @param ip IP地址
     * @param port 端口
     * @return 格式化的IP:端口字符串
     */
    private static String formatIpAndPort(String ip, int port) {
        return ip + ":" + port;
    }
}
