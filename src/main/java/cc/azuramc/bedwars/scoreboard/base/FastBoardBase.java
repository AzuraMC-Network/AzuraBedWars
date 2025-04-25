package cc.azuramc.bedwars.scoreboard.base;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

/**
 * 基于数据包的轻量级计分板API，专为Bukkit插件设计
 * 可以安全地异步使用，因为所有操作都在数据包级别进行
 * <p>
 * 项目地址: <a href="https://github.com/MrMicky-FR/FastBoard">GitHub</a>
 *
 * @author MrMicky
 * @version 2.1.4
 */
public abstract class FastBoardBase<T> {

    // 缓存数据包类与字段关系，提高反射效率
    private static final Map<Class<?>, Field[]> PACKETS = new HashMap<>(8);
    
    // 颜色代码数组，用于计分板行操作
    protected static final String[] COLOR_CODES = Arrays.stream(ChatColor.values())
            .map(Object::toString)
            .toArray(String[]::new);
    
    // 服务器版本类型
    private static final VersionType VERSION_TYPE;
    
    // NMS类和组件相关
    private static final Class<?> CHAT_COMPONENT_CLASS;
    private static final Class<?> CHAT_FORMAT_ENUM;
    private static final Object RESET_FORMATTING;
    private static final MethodHandle PLAYER_CONNECTION;
    private static final MethodHandle SEND_PACKET;
    private static final MethodHandle PLAYER_GET_HANDLE;
    private static final MethodHandle FIXED_NUMBER_FORMAT;
    
    // 计分板数据包构造器
    private static final FastReflection.PacketConstructor PACKET_SB_OBJ;
    private static final FastReflection.PacketConstructor PACKET_SB_DISPLAY_OBJ;
    private static final FastReflection.PacketConstructor PACKET_SB_TEAM;
    private static final FastReflection.PacketConstructor PACKET_SB_SERIALIZABLE_TEAM;
    private static final MethodHandle PACKET_SB_SET_SCORE;
    private static final MethodHandle PACKET_SB_RESET_SCORE;
    private static final boolean SCORE_OPTIONAL_COMPONENTS;
    
    // 计分板枚举和常量
    private static final Class<?> DISPLAY_SLOT_TYPE;
    private static final Class<?> ENUM_SB_HEALTH_DISPLAY;
    private static final Class<?> ENUM_SB_ACTION;
    private static final Class<?> ENUM_VISIBILITY;
    private static final Class<?> ENUM_COLLISION_RULE;
    private static final Object BLANK_NUMBER_FORMAT;
    private static final Object SIDEBAR_DISPLAY_SLOT;
    private static final Object ENUM_SB_HEALTH_DISPLAY_INTEGER;
    private static final Object ENUM_SB_ACTION_CHANGE;
    private static final Object ENUM_SB_ACTION_REMOVE;
    private static final Object ENUM_VISIBILITY_ALWAYS;
    private static final Object ENUM_COLLISION_RULE_ALWAYS;

    /**
     * 静态初始化块：初始化所有反射相关的字段和方法
     * 根据不同Minecraft版本进行适配
     */
    static {
        try {
            // 获取方法查找器
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            // 确定服务器版本类型
            if (FastReflection.isRepackaged()) {
                VERSION_TYPE = VersionType.V1_17;
            } else if (FastReflection.nmsOptionalClass(null, "ScoreboardServer$Action").isPresent()
                    || FastReflection.nmsOptionalClass(null, "ServerScoreboard$Method").isPresent()) {
                VERSION_TYPE = VersionType.V1_13;
            } else if (FastReflection.nmsOptionalClass(null, "IScoreboardCriteria$EnumScoreboardHealthDisplay").isPresent()
                    || FastReflection.nmsOptionalClass(null, "ObjectiveCriteria$RenderType").isPresent()) {
                VERSION_TYPE = VersionType.V1_8;
            } else {
                VERSION_TYPE = VersionType.V1_7;
            }

            // 获取NMS相关类
            String gameProtocolPackage = "network.protocol.game";
            Class<?> craftPlayerClass = FastReflection.obcClass("entity.CraftPlayer");
            Class<?> entityPlayerClass = FastReflection.nmsClass("server.level", "EntityPlayer", "ServerPlayer");
            Class<?> playerConnectionClass = FastReflection.nmsClass("server.network", "PlayerConnection", "ServerGamePacketListenerImpl");
            Class<?> packetClass = FastReflection.nmsClass("network.protocol", "Packet");
            Class<?> packetSbObjClass = FastReflection.nmsClass(gameProtocolPackage, "PacketPlayOutScoreboardObjective", "ClientboundSetObjectivePacket");
            Class<?> packetSbDisplayObjClass = FastReflection.nmsClass(gameProtocolPackage, "PacketPlayOutScoreboardDisplayObjective", "ClientboundSetDisplayObjectivePacket");
            Class<?> packetSbScoreClass = FastReflection.nmsClass(gameProtocolPackage, "PacketPlayOutScoreboardScore", "ClientboundSetScorePacket");
            Class<?> packetSbTeamClass = FastReflection.nmsClass(gameProtocolPackage, "PacketPlayOutScoreboardTeam", "ClientboundSetPlayerTeamPacket");
            Class<?> sbTeamClass = VersionType.V1_17.isHigherOrEqual()
                    ? FastReflection.innerClass(packetSbTeamClass, innerClass -> !innerClass.isEnum()) : null;
            
            // 获取PlayerConnection字段
            Field playerConnectionField = Arrays.stream(entityPlayerClass.getFields())
                    .filter(field -> field.getType().isAssignableFrom(playerConnectionClass))
                    .findFirst().orElseThrow(NoSuchFieldException::new);
            
            // 获取sendPacket方法
            Method sendPacketMethod = Stream.concat(
                            Arrays.stream(playerConnectionClass.getSuperclass().getMethods()),
                            Arrays.stream(playerConnectionClass.getMethods())
                    )
                    .filter(m -> m.getParameterCount() == 1 && m.getParameterTypes()[0] == packetClass)
                    .findFirst().orElseThrow(NoSuchMethodException::new);
            
            // 获取显示槽位枚举
            Optional<Class<?>> displaySlotEnum = FastReflection.nmsOptionalClass("world.scores", "DisplaySlot");
            
            // 初始化关键NMS类
            CHAT_COMPONENT_CLASS = FastReflection.nmsClass("network.chat", "IChatBaseComponent","Component");
            CHAT_FORMAT_ENUM = FastReflection.nmsClass(null, "EnumChatFormat", "ChatFormatting");
            DISPLAY_SLOT_TYPE = displaySlotEnum.orElse(int.class);
            RESET_FORMATTING = FastReflection.enumValueOf(CHAT_FORMAT_ENUM, "RESET", 21);
            SIDEBAR_DISPLAY_SLOT = displaySlotEnum.isPresent() ? FastReflection.enumValueOf(DISPLAY_SLOT_TYPE, "SIDEBAR", 1) : 1;
            
            // 初始化方法句柄
            PLAYER_GET_HANDLE = lookup.findVirtual(craftPlayerClass, "getHandle", MethodType.methodType(entityPlayerClass));
            PLAYER_CONNECTION = lookup.unreflectGetter(playerConnectionField);
            SEND_PACKET = lookup.unreflect(sendPacketMethod);
            PACKET_SB_OBJ = FastReflection.findPacketConstructor(packetSbObjClass, lookup);
            PACKET_SB_DISPLAY_OBJ = FastReflection.findPacketConstructor(packetSbDisplayObjClass, lookup);

            // 处理1.20.3+版本的数字格式
            Optional<Class<?>> numberFormat = FastReflection.nmsOptionalClass("network.chat.numbers", "NumberFormat");
            MethodHandle packetSbSetScore;
            MethodHandle packetSbResetScore = null;
            MethodHandle fixedFormatConstructor = null;
            Object blankNumberFormat = null;
            boolean scoreOptionalComponents = false;

            // 1.20.3+版本的特殊处理
            if (numberFormat.isPresent()) {
                Class<?> blankFormatClass = FastReflection.nmsClass("network.chat.numbers", "BlankFormat");
                Class<?> fixedFormatClass = FastReflection.nmsClass("network.chat.numbers", "FixedFormat");
                Class<?> resetScoreClass = FastReflection.nmsClass(gameProtocolPackage, "ClientboundResetScorePacket");
                
                // 创建方法类型
                MethodType scoreType = MethodType.methodType(void.class, String.class, String.class, int.class, CHAT_COMPONENT_CLASS, numberFormat.get());
                MethodType scoreTypeOptional = MethodType.methodType(void.class, String.class, String.class, int.class, Optional.class, Optional.class);
                MethodType removeScoreType = MethodType.methodType(void.class, String.class, String.class);
                MethodType fixedFormatType = MethodType.methodType(void.class, CHAT_COMPONENT_CLASS);
                
                // 查找BlankFormat字段
                Optional<Field> blankField = Arrays.stream(blankFormatClass.getFields()).filter(f -> f.getType() == blankFormatClass).findAny();
                
                // 检查1.20.5+版本是否使用Optional
                Optional<MethodHandle> optionalScorePacket = FastReflection.optionalConstructor(packetSbScoreClass, lookup, scoreTypeOptional);
                
                // 初始化方法句柄
                fixedFormatConstructor = lookup.findConstructor(fixedFormatClass, fixedFormatType);
                packetSbSetScore = optionalScorePacket.isPresent() ? optionalScorePacket.get()
                        : lookup.findConstructor(packetSbScoreClass, scoreType);
                scoreOptionalComponents = optionalScorePacket.isPresent();
                packetSbResetScore = lookup.findConstructor(resetScoreClass, removeScoreType);
                blankNumberFormat = blankField.isPresent() ? blankField.get().get(null) : null;
            } 
            // 1.17-1.20.2版本的处理
            else if (VersionType.V1_17.isHigherOrEqual()) {
                Class<?> enumSbAction = FastReflection.nmsClass("server", "ScoreboardServer$Action", "ServerScoreboard$Method");
                MethodType scoreType = MethodType.methodType(void.class, enumSbAction, String.class, String.class, int.class);
                packetSbSetScore = lookup.findConstructor(packetSbScoreClass, scoreType);
            } 
            // 旧版本的处理
            else {
                packetSbSetScore = lookup.findConstructor(packetSbScoreClass, MethodType.methodType(void.class));
            }

            // 保存初始化结果
            PACKET_SB_SET_SCORE = packetSbSetScore;
            PACKET_SB_RESET_SCORE = packetSbResetScore;
            PACKET_SB_TEAM = FastReflection.findPacketConstructor(packetSbTeamClass, lookup);
            PACKET_SB_SERIALIZABLE_TEAM = sbTeamClass == null ? null : FastReflection.findPacketConstructor(sbTeamClass, lookup);
            FIXED_NUMBER_FORMAT = fixedFormatConstructor;
            BLANK_NUMBER_FORMAT = blankNumberFormat;
            SCORE_OPTIONAL_COMPONENTS = scoreOptionalComponents;

            // 1.17+版本的碰撞和可见性规则
            if (VersionType.V1_17.isHigherOrEqual()) {
                ENUM_VISIBILITY = FastReflection.nmsClass("world.scores", "ScoreboardTeamBase$EnumNameTagVisibility", "Team$Visibility");
                ENUM_COLLISION_RULE = FastReflection.nmsClass("world.scores", "ScoreboardTeamBase$EnumTeamPush", "Team$CollisionRule");
                ENUM_VISIBILITY_ALWAYS = FastReflection.enumValueOf(ENUM_VISIBILITY, "ALWAYS", 0);
                ENUM_COLLISION_RULE_ALWAYS = FastReflection.enumValueOf(ENUM_COLLISION_RULE, "ALWAYS", 0);
            } else {
                ENUM_VISIBILITY = null;
                ENUM_COLLISION_RULE = null;
                ENUM_VISIBILITY_ALWAYS = null;
                ENUM_COLLISION_RULE_ALWAYS = null;
            }

            // 缓存数据包字段，提高反射性能
            for (Class<?> clazz : Arrays.asList(packetSbObjClass, packetSbDisplayObjClass, packetSbScoreClass, packetSbTeamClass, sbTeamClass)) {
                if (clazz == null) {
                    continue;
                }
                Field[] fields = Arrays.stream(clazz.getDeclaredFields())
                        .filter(field -> !Modifier.isStatic(field.getModifiers()))
                        .toArray(Field[]::new);
                for (Field field : fields) {
                    field.setAccessible(true);
                }
                PACKETS.put(clazz, fields);
            }

            // 初始化1.8+版本特有的枚举
            if (VersionType.V1_8.isHigherOrEqual()) {
                String enumSbActionClass = VersionType.V1_13.isHigherOrEqual()
                        ? "ScoreboardServer$Action"
                        : "PacketPlayOutScoreboardScore$EnumScoreboardAction";
                ENUM_SB_HEALTH_DISPLAY = FastReflection.nmsClass("world.scores.criteria", "IScoreboardCriteria$EnumScoreboardHealthDisplay", "ObjectiveCriteria$RenderType");
                ENUM_SB_ACTION = FastReflection.nmsClass("server", enumSbActionClass, "ServerScoreboard$Method");
                ENUM_SB_HEALTH_DISPLAY_INTEGER = FastReflection.enumValueOf(ENUM_SB_HEALTH_DISPLAY, "INTEGER", 0);
                ENUM_SB_ACTION_CHANGE = FastReflection.enumValueOf(ENUM_SB_ACTION, "CHANGE", 0);
                ENUM_SB_ACTION_REMOVE = FastReflection.enumValueOf(ENUM_SB_ACTION, "REMOVE", 1);
            } else {
                ENUM_SB_HEALTH_DISPLAY = null;
                ENUM_SB_ACTION = null;
                ENUM_SB_HEALTH_DISPLAY_INTEGER = null;
                ENUM_SB_ACTION_CHANGE = null;
                ENUM_SB_ACTION_REMOVE = null;
            }
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    // 计分板相关实例变量
    private final Player player;
    private final String id;
    private final List<T> lines = new ArrayList<>();
    private final List<T> scores = new ArrayList<>();
    private T title = emptyLine();
    private boolean deleted = false;

    /**
     * 创建一个新的FastBoard实例
     *
     * @param player 计分板的所有者（玩家）
     */
    protected FastBoardBase(Player player) {
        this.player = Objects.requireNonNull(player, "player不能为null");
        this.id = "fb-" + Integer.toHexString(ThreadLocalRandom.current().nextInt());

        try {
            // 创建计分板目标并设置显示位置
            sendObjectivePacket(ObjectiveMode.CREATE);
            sendDisplayObjectivePacket();
        } catch (Throwable t) {
            throw new RuntimeException("无法创建计分板", t);
        }
    }

    /**
     * 获取计分板标题
     *
     * @return 计分板标题
     */
    public T getTitle() {
        return this.title;
    }

    /**
     * 更新计分板标题
     *
     * @param title 新的计分板标题
     * @throws IllegalArgumentException 如果1.12或更低版本中标题超过32个字符
     * @throws IllegalStateException    如果计分板已被删除
     */
    public void updateTitle(T title) {
        if (this.title.equals(Objects.requireNonNull(title, "title不能为null"))) {
            return;
        }

        this.title = title;

        try {
            sendObjectivePacket(ObjectiveMode.UPDATE);
        } catch (Throwable t) {
            throw new RuntimeException("无法更新计分板标题", t);
        }
    }

    /**
     * 获取计分板的所有行
     *
     * @return 计分板行列表的副本
     */
    public List<T> getLines() {
        return new ArrayList<>(this.lines);
    }

    /**
     * 获取指定的计分板行
     *
     * @param line 行号
     * @return 该行的内容
     * @throws IndexOutOfBoundsException 如果行号超出范围
     */
    public T getLine(int line) {
        checkLineNumber(line, true, false);
        return this.lines.get(line);
    }

    /**
     * 获取特定行的分数显示文本
     * 注意：在1.20.2及以下版本中，返回值不会被使用
     *
     * @param line 行号
     * @return 分数显示文本，可能为空
     * @throws IndexOutOfBoundsException 如果行号超出范围
     */
    public Optional<T> getScore(int line) {
        checkLineNumber(line, true, false);
        return Optional.ofNullable(this.scores.get(line));
    }

    /**
     * 更新单个计分板行的内容
     *
     * @param line 行号
     * @param text 新的行文本
     * @throws IndexOutOfBoundsException 如果行号超出范围
     */
    public synchronized void updateLine(int line, T text) {
        updateLine(line, text, null);
    }

    /**
     * 更新单个计分板行的内容和分数显示
     * 分数显示仅在1.20.3及更高版本生效
     *
     * @param line 行号
     * @param text 新的行文本
     * @param scoreText 新的分数显示文本，如果为null则不改变当前值
     * @throws IndexOutOfBoundsException 如果行号超出范围
     */
    public synchronized void updateLine(int line, T text, T scoreText) {
        checkLineNumber(line, false, false);

        try {
            // 如果是更新现有行
            if (line < size()) {
                this.lines.set(line, text);
                this.scores.set(line, scoreText);

                sendLineChange(getScoreByLine(line));

                if (customScoresSupported()) {
                    sendScorePacket(getScoreByLine(line), ScoreboardAction.CHANGE);
                }

                return;
            }

            // 如果是添加新行
            List<T> newLines = new ArrayList<>(this.lines);
            List<T> newScores = new ArrayList<>(this.scores);

            // 如果需要填充空行
            if (line > size()) {
                for (int i = size(); i < line; i++) {
                    newLines.add(emptyLine());
                    newScores.add(null);
                }
            }

            // 添加新行
            newLines.add(text);
            newScores.add(scoreText);

            updateLines(newLines, newScores);
        } catch (Throwable t) {
            throw new RuntimeException("无法更新计分板行", t);
        }
    }

    /**
     * 移除计分板行
     *
     * @param line 要移除的行号
     */
    public synchronized void removeLine(int line) {
        checkLineNumber(line, false, false);

        if (line >= size()) {
            return;
        }

        List<T> newLines = new ArrayList<>(this.lines);
        List<T> newScores = new ArrayList<>(this.scores);
        newLines.remove(line);
        newScores.remove(line);
        updateLines(newLines, newScores);
    }

    /**
     * 更新所有计分板行
     *
     * @param lines 新的行内容数组
     * @throws IllegalArgumentException 如果1.12或更低版本中任一行超过30个字符
     * @throws IllegalStateException    如果计分板已被删除
     */
    public void updateLines(T... lines) {
        updateLines(Arrays.asList(lines));
    }

    /**
     * 更新所有计分板行
     *
     * @param lines 新的行内容集合
     * @throws IllegalArgumentException 如果1.12或更低版本中任一行超过30个字符
     * @throws IllegalStateException    如果计分板已被删除
     */
    public synchronized void updateLines(Collection<T> lines) {
        updateLines(lines, null);
    }

    /**
     * 更新所有计分板行及其分数显示文本
     * 分数显示仅在1.20.3及更高版本服务器上可见
     *
     * @param lines 新的计分板行内容
     * @param scores 每行分数的显示文本，如果为null则使用默认值（空白）
     * @throws IllegalArgumentException 如果1.12或更低版本中任一行超过30个字符
     * @throws IllegalArgumentException 如果lines与scores的大小不一致
     * @throws IllegalStateException    如果计分板已被删除
     */
    public synchronized void updateLines(Collection<T> lines, Collection<T> scores) {
        Objects.requireNonNull(lines, "lines不能为null");
        checkLineNumber(lines.size(), false, true);

        if (scores != null && scores.size() != lines.size()) {
            throw new IllegalArgumentException("分数集合的大小必须与计分板行数相匹配");
        }

        // 保存旧数据用于比较
        List<T> oldLines = new ArrayList<>(this.lines);
        this.lines.clear();
        this.lines.addAll(lines);

        List<T> oldScores = new ArrayList<>(this.scores);
        this.scores.clear();
        this.scores.addAll(scores != null ? scores : Collections.nCopies(lines.size(), null));

        int linesSize = this.lines.size();

        try {
            // 如果行数发生变化，需要添加或删除团队和分数
            if (oldLines.size() != linesSize) {
                List<T> oldLinesCopy = new ArrayList<>(oldLines);

                // 如果减少了行数
                if (oldLines.size() > linesSize) {
                    for (int i = oldLinesCopy.size(); i > linesSize; i--) {
                        sendTeamPacket(i - 1, TeamMode.REMOVE);
                        sendScorePacket(i - 1, ScoreboardAction.REMOVE);
                        oldLines.removeFirst();
                    }
                } 
                // 如果增加了行数
                else {
                    for (int i = oldLinesCopy.size(); i < linesSize; i++) {
                        sendScorePacket(i, ScoreboardAction.CHANGE);
                        sendTeamPacket(i, TeamMode.CREATE, null, null);
                    }
                }
            }

            // 更新发生变化的行和分数
            for (int i = 0; i < linesSize; i++) {
                if (!Objects.equals(getLineByScore(oldLines, i), getLineByScore(i))) {
                    sendLineChange(i);
                }
                if (!Objects.equals(getLineByScore(oldScores, i), getLineByScore(this.scores, i))) {
                    sendScorePacket(i, ScoreboardAction.CHANGE);
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException("无法更新计分板行", t);
        }
    }

    /**
     * 更新指定行的分数显示文本
     * 分数显示仅在1.20.3及更高版本服务器上可见
     *
     * @param line 行号
     * @param text 要显示的分数文本，如果为null则使用默认值（空白）
     * @throws IllegalArgumentException 如果行号超出范围
     * @throws IllegalStateException    如果计分板已被删除
     */
    public synchronized void updateScore(int line, T text) {
        checkLineNumber(line, true, false);

        this.scores.set(line, text);

        try {
            if (customScoresSupported()) {
                sendScorePacket(getScoreByLine(line), ScoreboardAction.CHANGE);
            }
        } catch (Throwable e) {
            throw new RuntimeException("无法更新行分数", e);
        }
    }

    /**
     * 重置指定行的分数显示为默认值（空白）
     * 分数显示仅在1.20.3及更高版本服务器上可见
     *
     * @param line 行号
     * @throws IllegalArgumentException 如果行号超出范围
     * @throws IllegalStateException    如果计分板已被删除
     */
    public synchronized void removeScore(int line) {
        updateScore(line, null);
    }

    /**
     * 更新所有行的分数显示文本
     * 分数显示仅在1.20.3及更高版本服务器上可见
     *
     * @param texts 分数显示文本数组，null值会重置为默认值（空白）
     * @throws IllegalArgumentException 如果文本数组大小与当前计分板行数不匹配
     * @throws IllegalStateException    如果计分板已被删除
     */
    public synchronized void updateScores(T... texts) {
        updateScores(Arrays.asList(texts));
    }

    /**
     * 更新所有行的分数显示文本
     * 分数显示仅在1.20.3及更高版本服务器上可见
     *
     * @param texts 分数显示文本集合，null值会重置为默认值（空白）
     * @throws IllegalArgumentException 如果文本集合大小与当前计分板行数不匹配
     * @throws IllegalStateException    如果计分板已被删除
     */
    public synchronized void updateScores(Collection<T> texts) {
        Objects.requireNonNull(texts, "texts不能为null");

        if (this.scores.size() != this.lines.size()) {
            throw new IllegalArgumentException("分数集合的大小必须与计分板行数相匹配");
        }

        List<T> newScores = new ArrayList<>(texts);
        for (int i = 0; i < this.scores.size(); i++) {
            if (Objects.equals(this.scores.get(i), newScores.get(i))) {
                continue;
            }

            this.scores.set(i, newScores.get(i));

            try {
                if (customScoresSupported()) {
                    sendScorePacket(getScoreByLine(i), ScoreboardAction.CHANGE);
                }
            } catch (Throwable e) {
                throw new RuntimeException("无法更新分数", e);
            }
        }
    }

    /**
     * 获取计分板所属的玩家
     *
     * @return 拥有此计分板的玩家
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * 获取计分板ID
     *
     * @return 计分板ID
     */
    public String getId() {
        return this.id;
    }

    /**
     * 检查计分板是否已被删除
     *
     * @return 如果计分板已被删除则返回true
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    /**
     * 检查服务器是否支持自定义计分板分数（仅1.20.3+服务器支持）
     *
     * @return 如果服务器支持自定义分数则返回true
     */
    public boolean customScoresSupported() {
        return BLANK_NUMBER_FORMAT != null;
    }

    /**
     * 获取计分板大小（行数）
     *
     * @return 计分板行数
     */
    public int size() {
        return this.lines.size();
    }

    /**
     * 删除此计分板，如果玩家在线，将移除其计分板显示
     * 删除后，使用{@link #updateLines}和{@link #updateTitle}将抛出{@link IllegalStateException}
     *
     * @throws IllegalStateException 如果计分板已被删除
     */
    public void delete() {
        try {
            // 删除所有团队和目标
            for (int i = 0; i < this.lines.size(); i++) {
                sendTeamPacket(i, TeamMode.REMOVE);
            }

            sendObjectivePacket(ObjectiveMode.REMOVE);
        } catch (Throwable t) {
            throw new RuntimeException("无法删除计分板", t);
        }

        this.deleted = true;
    }

    /**
     * 子类需实现：发送行更改
     * 
     * @param score 行对应的分数
     * @throws Throwable 如果发送过程中出错
     */
    protected abstract void sendLineChange(int score) throws Throwable;

    /**
     * 子类需实现：将值转换为Minecraft组件
     * 
     * @param value 要转换的值
     * @return Minecraft组件
     * @throws Throwable 如果转换过程中出错
     */
    protected abstract Object toMinecraftComponent(T value) throws Throwable;

    /**
     * 子类需实现：序列化行内容
     * 
     * @param value 要序列化的值
     * @return 序列化后的字符串
     */
    protected abstract String serializeLine(T value);

    /**
     * 子类需实现：创建空行
     * 
     * @return 表示空行的值
     */
    protected abstract T emptyLine();

    /**
     * 检查行号是否有效
     * 
     * @param line 行号
     * @param checkInRange 是否检查是否在当前行范围内
     * @param checkMax 是否检查是否超过最大行数
     * @throws IllegalArgumentException 如果行号无效
     */
    private void checkLineNumber(int line, boolean checkInRange, boolean checkMax) {
        if (line < 0) {
            throw new IllegalArgumentException("行号必须为正数");
        }

        if (checkInRange && line >= this.lines.size()) {
            throw new IllegalArgumentException("行号必须小于 " + this.lines.size());
        }

        if (checkMax && line >= COLOR_CODES.length - 1) {
            throw new IllegalArgumentException("行号过高: " + line);
        }
    }

    /**
     * 获取指定行对应的分数
     * 
     * @param line 行号
     * @return 分数
     */
    protected int getScoreByLine(int line) {
        return this.lines.size() - line - 1;
    }

    /**
     * 根据分数获取对应的行内容
     * 
     * @param score 分数
     * @return 行内容
     */
    protected T getLineByScore(int score) {
        return getLineByScore(this.lines, score);
    }

    /**
     * 从指定列表中根据分数获取对应的行内容
     * 
     * @param lines 行内容列表
     * @param score 分数
     * @return 行内容，如果分数超出范围则返回null
     */
    protected T getLineByScore(List<T> lines, int score) {
        return score < lines.size() ? lines.get(lines.size() - score - 1) : null;
    }

    /**
     * 发送目标数据包（创建/更新/删除计分板）
     * 
     * @param mode 操作模式
     * @throws Throwable 如果发送过程中出错
     */
    protected void sendObjectivePacket(ObjectiveMode mode) throws Throwable {
        Object packet = PACKET_SB_OBJ.invoke();

        setField(packet, String.class, this.id);
        setField(packet, int.class, mode.ordinal());

        if (mode != ObjectiveMode.REMOVE) {
            setComponentField(packet, this.title, 1);
            setField(packet, Optional.class, Optional.empty()); // 1.20.5+版本的数字格式，之前为可null

            if (VersionType.V1_8.isHigherOrEqual()) {
                setField(packet, ENUM_SB_HEALTH_DISPLAY, ENUM_SB_HEALTH_DISPLAY_INTEGER);
            }
        } else if (VERSION_TYPE == VersionType.V1_7) {
            setField(packet, String.class, "", 1);
        }

        sendPacket(packet);
    }

    /**
     * 发送显示目标数据包（设置计分板显示位置）
     * 
     * @throws Throwable 如果发送过程中出错
     */
    protected void sendDisplayObjectivePacket() throws Throwable {
        Object packet = PACKET_SB_DISPLAY_OBJ.invoke();

        setField(packet, DISPLAY_SLOT_TYPE, SIDEBAR_DISPLAY_SLOT); // 位置
        setField(packet, String.class, this.id); // 计分板名称

        sendPacket(packet);
    }

    /**
     * 发送分数数据包（更新/删除分数）
     * 
     * @param score 分数
     * @param action 操作（更新/删除）
     * @throws Throwable 如果发送过程中出错
     */
    protected void sendScorePacket(int score, ScoreboardAction action) throws Throwable {
        if (VersionType.V1_17.isHigherOrEqual()) {
            sendModernScorePacket(score, action);
            return;
        }

        Object packet = PACKET_SB_SET_SCORE.invoke();

        setField(packet, String.class, COLOR_CODES[score], 0); // 玩家名称

        if (VersionType.V1_8.isHigherOrEqual()) {
            Object enumAction = action == ScoreboardAction.REMOVE
                    ? ENUM_SB_ACTION_REMOVE : ENUM_SB_ACTION_CHANGE;
            setField(packet, ENUM_SB_ACTION, enumAction);
        } else {
            setField(packet, int.class, action.ordinal(), 1); // 操作
        }

        if (action == ScoreboardAction.CHANGE) {
            setField(packet, String.class, this.id, 1); // 目标名称
            setField(packet, int.class, score); // 分数
        }

        sendPacket(packet);
    }

    /**
     * 发送现代版本的分数数据包（1.17+）
     * 
     * @param score 分数
     * @param action 操作（更新/删除）
     * @throws Throwable 如果发送过程中出错
     */
    private void sendModernScorePacket(int score, ScoreboardAction action) throws Throwable {
        String objName = COLOR_CODES[score];
        Object enumAction = action == ScoreboardAction.REMOVE
                ? ENUM_SB_ACTION_REMOVE : ENUM_SB_ACTION_CHANGE;

        // 1.20.3之前的版本
        if (PACKET_SB_RESET_SCORE == null) {
            sendPacket(PACKET_SB_SET_SCORE.invoke(enumAction, this.id, objName, score));
            return;
        }

        // 删除分数
        if (action == ScoreboardAction.REMOVE) {
            sendPacket(PACKET_SB_RESET_SCORE.invoke(objName, this.id));
            return;
        }

        // 更新分数
        T scoreFormat = getLineByScore(this.scores, score);
        Object format = scoreFormat != null
                ? FIXED_NUMBER_FORMAT.invoke(toMinecraftComponent(scoreFormat))
                : BLANK_NUMBER_FORMAT;
        Object scorePacket = SCORE_OPTIONAL_COMPONENTS
                ? PACKET_SB_SET_SCORE.invoke(objName, this.id, score, Optional.empty(), Optional.of(format))
                : PACKET_SB_SET_SCORE.invoke(objName, this.id, score, null, format);

        sendPacket(scorePacket);
    }

    /**
     * 发送团队数据包（创建/更新/删除团队）
     * 
     * @param score 分数
     * @param mode 操作模式
     * @throws Throwable 如果发送过程中出错
     */
    protected void sendTeamPacket(int score, TeamMode mode) throws Throwable {
        sendTeamPacket(score, mode, null, null);
    }

    /**
     * 发送团队数据包（创建/更新/删除团队）
     * 
     * @param score 分数
     * @param mode 操作模式
     * @param prefix 前缀
     * @param suffix 后缀
     * @throws Throwable 如果发送过程中出错
     */
    protected void sendTeamPacket(int score, TeamMode mode, T prefix, T suffix)
            throws Throwable {
        if (mode == TeamMode.ADD_PLAYERS || mode == TeamMode.REMOVE_PLAYERS) {
            throw new UnsupportedOperationException();
        }

        Object packet = PACKET_SB_TEAM.invoke();

        setField(packet, String.class, this.id + ':' + score); // 团队名称
        setField(packet, int.class, mode.ordinal(), VERSION_TYPE == VersionType.V1_8 ? 1 : 0); // 更新模式

        if (mode == TeamMode.REMOVE) {
            sendPacket(packet);
            return;
        }

        // 1.17+版本使用可序列化团队
        if (VersionType.V1_17.isHigherOrEqual()) {
            Object team = PACKET_SB_SERIALIZABLE_TEAM.invoke();
            // 由于数据包初始化为null值，需要设置更多字段
            setComponentField(team, null, 0); // 显示名称
            setField(team, CHAT_FORMAT_ENUM, RESET_FORMATTING); // 颜色
            setComponentField(team, prefix, 1); // 前缀
            setComponentField(team, suffix, 2); // 后缀
            setField(team, String.class, "always", 0); // 1.21.5之前的可见性
            setField(team, String.class, "always", 1); // 1.21.5之前的碰撞
            setField(team, ENUM_VISIBILITY, ENUM_VISIBILITY_ALWAYS, 0); // 1.21.5+可见性
            setField(team, ENUM_COLLISION_RULE, ENUM_COLLISION_RULE_ALWAYS, 0); // 1.21.5+碰撞
            setField(packet, Optional.class, Optional.of(team));
        } else {
            setComponentField(packet, prefix, 2); // 前缀
            setComponentField(packet, suffix, 3); // 后缀
            setField(packet, String.class, "always", 4); // 1.8+的可见性
            setField(packet, String.class, "always", 5); // 1.9+的碰撞
        }

        if (mode == TeamMode.CREATE) {
            setField(packet, Collection.class, Collections.singletonList(COLOR_CODES[score])); // 团队中的玩家
        }

        sendPacket(packet);
    }

    /**
     * 发送数据包到玩家客户端
     * 
     * @param packet 要发送的数据包
     * @throws Throwable 如果发送过程中出错
     */
    private void sendPacket(Object packet) throws Throwable {
        if (this.deleted) {
            throw new IllegalStateException("此计分板已被删除");
        }

        if (this.player.isOnline()) {
            Object entityPlayer = PLAYER_GET_HANDLE.invoke(this.player);
            Object playerConnection = PLAYER_CONNECTION.invoke(entityPlayer);
            SEND_PACKET.invoke(playerConnection, packet);
        }
    }

    /**
     * 设置数据包的字段值
     * 
     * @param object 目标对象
     * @param fieldType 字段类型
     * @param value 要设置的值
     * @throws ReflectiveOperationException 如果设置过程中出错
     */
    private void setField(Object object, Class<?> fieldType, Object value)
            throws ReflectiveOperationException {
        setField(object, fieldType, value, 0);
    }

    /**
     * 设置数据包的字段值
     * 
     * @param packet 目标数据包
     * @param fieldType 字段类型
     * @param value 要设置的值
     * @param count 匹配计数（第几个该类型的字段）
     * @throws ReflectiveOperationException 如果设置过程中出错
     */
    private void setField(Object packet, Class<?> fieldType, Object value, int count)
            throws ReflectiveOperationException {
        int i = 0;
        for (Field field : PACKETS.get(packet.getClass())) {
            if (field.getType() == fieldType && count == i++) {
                field.set(packet, value);
            }
        }
    }

    /**
     * 设置组件字段的值
     * 
     * @param packet 目标数据包
     * @param value 要设置的值
     * @param count 匹配计数（第几个该类型的字段）
     * @throws Throwable 如果设置过程中出错
     */
    private void setComponentField(Object packet, T value, int count) throws Throwable {
        // 1.13之前版本使用字符串
        if (!VersionType.V1_13.isHigherOrEqual()) {
            String line = value != null ? serializeLine(value) : "";
            setField(packet, String.class, line, count);
            return;
        }

        // 1.13+版本使用组件
        int i = 0;
        for (Field field : PACKETS.get(packet.getClass())) {
            if ((field.getType() == String.class || field.getType() == CHAT_COMPONENT_CLASS) && count == i++) {
                field.set(packet, toMinecraftComponent(value));
            }
        }
    }

    /**
     * 计分板目标操作模式枚举
     */
    public enum ObjectiveMode {
        /** 创建计分板 */
        CREATE, 
        /** 移除计分板 */
        REMOVE, 
        /** 更新计分板 */
        UPDATE
    }

    /**
     * 团队操作模式枚举
     */
    public enum TeamMode {
        /** 创建团队 */
        CREATE, 
        /** 移除团队 */
        REMOVE, 
        /** 更新团队 */
        UPDATE, 
        /** 添加玩家到团队 */
        ADD_PLAYERS, 
        /** 从团队移除玩家 */
        REMOVE_PLAYERS
    }

    /**
     * 计分板分数操作枚举
     */
    public enum ScoreboardAction {
        /** 更改分数 */
        CHANGE, 
        /** 移除分数 */
        REMOVE
    }

    /**
     * 版本类型枚举
     */
    enum VersionType {
        /** 1.7.x 版本 */
        V1_7, 
        /** 1.8.x - 1.12.x 版本 */
        V1_8, 
        /** 1.13.x - 1.16.x 版本 */
        V1_13, 
        /** 1.17+ 版本 */
        V1_17;

        /**
         * 检查当前服务器版本是否大于等于此版本
         * 
         * @return 如果当前服务器版本大于等于此版本则返回true
         */
        public boolean isHigherOrEqual() {
            return VERSION_TYPE.ordinal() >= ordinal();
        }
    }
}
