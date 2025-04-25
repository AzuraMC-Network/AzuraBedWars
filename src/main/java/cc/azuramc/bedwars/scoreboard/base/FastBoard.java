package cc.azuramc.bedwars.scoreboard.base;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.util.Objects;

/**
 * 基于字符串的记分板实现，继承自FastBoardBase
 * 提供简单易用的API，用于创建和管理玩家记分板
 */
public class FastBoard extends FastBoardBase<String> {

    // 反射相关常量
    private static final MethodHandle MESSAGE_FROM_STRING;
    private static final Object EMPTY_MESSAGE;

    /*
      静态初始化块：设置转换字符串到Minecraft聊天组件的方法句柄
     */
    static {
        try {
            // 获取方法查找器
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            
            // 获取CraftChatMessage类和fromString方法
            Class<?> craftChatMessageClass = FastReflection.obcClass("util.CraftChatMessage");
            MESSAGE_FROM_STRING = lookup.unreflect(craftChatMessageClass.getMethod("fromString", String.class));
            
            // 创建空消息对象
            EMPTY_MESSAGE = Array.get(MESSAGE_FROM_STRING.invoke(""), 0);
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    /**
     * 创建一个新的FastBoard实例
     * 
     * @param player 计分板所属的玩家
     */
    public FastBoard(Player player) {
        super(player);
    }

    /**
     * 更新计分板标题
     * 在1.12及更低版本中，标题长度不能超过32个字符
     *
     * @param title 新的计分板标题
     * @throws NullPointerException 如果标题为null
     * @throws IllegalArgumentException 如果在旧版本中标题长度超过32个字符
     */
    @Override
    public void updateTitle(String title) {
        Objects.requireNonNull(title, "title不能为null");

        // 在1.12及更低版本中检查标题长度
        if (!VersionType.V1_13.isHigherOrEqual() && title.length() > 32) {
            throw new IllegalArgumentException("标题长度不能超过32个字符");
        }

        super.updateTitle(title);
    }

    /**
     * 更新计分板行内容
     * 在1.12及更低版本中，每行长度不能超过30个字符
     *
     * @param lines 新的行内容数组
     * @throws NullPointerException 如果lines为null
     * @throws IllegalArgumentException 如果在旧版本中某行长度超过30个字符
     */
    @Override
    public void updateLines(String... lines) {
        Objects.requireNonNull(lines, "lines不能为null");

        // 在1.12及更低版本中检查行长度
        if (!VersionType.V1_13.isHigherOrEqual()) {
            int lineCount = 0;
            for (String line : lines) {
                if (line != null && line.length() > 30) {
                    throw new IllegalArgumentException("第" + lineCount + "行长度不能超过30个字符");
                }
                lineCount++;
            }
        }

        super.updateLines(lines);
    }

    /**
     * 发送行更改数据包
     * 根据Minecraft版本处理行内容，包括颜色代码和长度限制
     *
     * @param score 行对应的分数
     * @throws Throwable 如果发送过程中出错
     */
    @Override
    protected void sendLineChange(int score) throws Throwable {
        // 根据版本确定最大长度
        int maxLength = hasLinesMaxLength() ? 16 : 1024;
        String line = getLineByScore(score);
        String prefix;
        String suffix = "";

        // 处理空行或短行
        if (line == null || line.isEmpty()) {
            prefix = COLOR_CODES[score] + ChatColor.RESET;
        } else if (line.length() <= maxLength) {
            prefix = line;
        } else {
            // 处理长行，需要分割为前缀和后缀，避免分割颜色代码
            int index = line.charAt(maxLength - 1) == ChatColor.COLOR_CHAR
                    ? (maxLength - 1) : maxLength;
            prefix = line.substring(0, index);
            String suffixTmp = line.substring(index);
            ChatColor chatColor = null;

            // 检查后缀是否以颜色代码开始
            if (suffixTmp.length() >= 2 && suffixTmp.charAt(0) == ChatColor.COLOR_CHAR) {
                chatColor = ChatColor.getByChar(suffixTmp.charAt(1));
            }

            // 确保后缀使用正确的颜色
            String color = ChatColor.getLastColors(prefix);
            boolean addColor = chatColor == null || chatColor.isFormat();

            suffix = (addColor ? (color.isEmpty() ? ChatColor.RESET.toString() : color) : "") + suffixTmp;
        }

        // 防止客户端崩溃，确保长度在限制内
        if (prefix.length() > maxLength || suffix.length() > maxLength) {
            prefix = prefix.substring(0, Math.min(maxLength, prefix.length()));
            suffix = suffix.substring(0, Math.min(maxLength, suffix.length()));
        }

        // 发送团队数据包更新行内容
        sendTeamPacket(score, TeamMode.UPDATE, prefix, suffix);
    }

    /**
     * 将字符串转换为Minecraft聊天组件
     *
     * @param line 要转换的字符串
     * @return Minecraft聊天组件对象
     * @throws Throwable 如果转换过程中出错
     */
    @Override
    protected Object toMinecraftComponent(String line) throws Throwable {
        if (line == null || line.isEmpty()) {
            return EMPTY_MESSAGE;
        }

        return Array.get(MESSAGE_FROM_STRING.invoke(line), 0);
    }

    /**
     * 序列化行内容
     * 由于FastBoard直接使用字符串，此方法直接返回输入值
     *
     * @param value 要序列化的字符串
     * @return 序列化后的字符串（原字符串）
     */
    @Override
    protected String serializeLine(String value) {
        return value;
    }

    /**
     * 创建空行
     *
     * @return 表示空行的字符串
     */
    @Override
    protected String emptyLine() {
        return "";
    }

    /**
     * 检查玩家是否受前缀/后缀字符数限制
     * 默认情况下，在1.12或更低版本中返回true
     * 可以重写此方法以修复与某些版本支持插件的兼容性
     *
     * @return 如果玩家受到字符数限制则返回true
     */
    protected boolean hasLinesMaxLength() {
        return !VersionType.V1_13.isHigherOrEqual();
    }
}
