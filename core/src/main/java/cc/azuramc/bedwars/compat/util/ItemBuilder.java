package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 物品构建工具类
 * 用于快速创建和配置各种物品
 * 提供跨版本兼容性支持(1.8-1.19+)
 * @author an5w1r@163.com
 */
public class ItemBuilder {
    private ItemStack itemStack;
    
    // 反射缓存
    private static Class<?> craftItemStackClass;
    private static Class<?> nmsItemStackClass;
    private static Class<?> nbtTagCompoundClass;
    private static Method asNMSCopyMethod;
    private static Method getTagMethod;
    private static Method setBooleanMethod;
    private static Method setTagMethod;
    private static Method asBukkitCopyMethod;
    private static boolean reflectionInitialized = false;
    
    // 静态初始化块，在类首次加载时初始化反射缓存
    static {
        if (VersionUtil.isVersion1_8()) {
            try {
                craftItemStackClass = Class.forName("org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack");
                nmsItemStackClass = Class.forName("net.minecraft.server.v1_8_R3.ItemStack");
                nbtTagCompoundClass = Class.forName("net.minecraft.server.v1_8_R3.NBTTagCompound");
                
                asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
                getTagMethod = nmsItemStackClass.getMethod("getTag");
                setBooleanMethod = nbtTagCompoundClass.getMethod("setBoolean", String.class, boolean.class);
                setTagMethod = nmsItemStackClass.getMethod("setTag", nbtTagCompoundClass);
                asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
                
                reflectionInitialized = true;
            } catch (Exception e) {
                // 记录初始化失败，稍后会回退到备用方法
                LoggerUtil.warn("无法初始化NMS反射: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 创建空物品构建器
     */
    public ItemBuilder() {
        if (XMaterial.AIR.get() != null) {
            itemStack = new ItemStack(XMaterial.AIR.get());
        }
    }
    
    /**
     * 创建指定类型的物品构建器
     * @param material 物品类型
     */
    public ItemBuilder(Material material) {
        itemStack = new ItemStack(material);
    }
    
    /**
     * 创建基于现有物品的构建器
     * @param itemStack 现有物品
     */
    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    /**
     * 获取玩家头颅的Material，兼容新旧版本
     * @return 玩家头颅的Material
     */
    public static Material getPlayerSkullMaterial() {
        return XMaterial.PLAYER_HEAD.get();
    }

    /**
     * 设置物品类型
     * @param material 物品类型
     * @return 构建器实例
     */
    public ItemBuilder setType(Material material) {
        itemStack.setType(material);
        return this;
    }

    /**
     * 设置物品堆叠
     * @param itemStack 物品堆叠
     * @return 构建器实例
     */
    public ItemBuilder setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        return this;
    }

    /**
     * 设置头颅所有者
     * @param owner 所有者名称或UUID字符串
     * @return 构建器实例
     */
    @SuppressWarnings("deprecation")
    public ItemBuilder setOwner(String owner) {
        Material skullMaterial = getPlayerSkullMaterial();
        itemStack.setType(skullMaterial);
        
        // 为旧版本设置数据值
        if (VersionUtil.isLessThan1_13()) {
            setDurabilityCompat(itemStack, (short) 3);
        }

        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        if (skullMeta == null) {
            return this;
        }
        
        try {
            // 尝试使用UUID
            OfflinePlayer offlinePlayer;
            try {
                UUID uuid = UUID.fromString(owner);
                offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            } catch (IllegalArgumentException e) {
                // 如果不是UUID，使用名称
                offlinePlayer = Bukkit.getOfflinePlayer(owner);
            }
            
            // 1.13+使用setOwningPlayer方法
            if (!VersionUtil.isLessThan1_13()) {
                Method setOwningPlayer = SkullMeta.class.getMethod("setOwningPlayer", OfflinePlayer.class);
                setOwningPlayer.invoke(skullMeta, offlinePlayer);
            } else {
                // 1.8-1.12使用setOwner方法
                skullMeta.setOwner(owner);
            }
        } catch (Exception e) {
            // 忽略异常，继续处理
        }
        
        itemStack.setItemMeta(skullMeta);
        return this;
    }

    /**
     * 设置物品是否无法破坏
     * @param unbreakable 是否无法破坏
     * @return 构建器实例
     */
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        return setUnbreakable(unbreakable, false);
    }

    /**
     * 设置物品是否无法破坏
     * @param unbreakable 是否无法破坏
     * @param hide 是否隐藏无法破坏标签
     * @return 构建器实例
     */
    public ItemBuilder setUnbreakable(boolean unbreakable, boolean hide) {
        try {
            // 优先使用NMS方法设置Unbreakable标签（适用于1.8）
            if (VersionUtil.isVersion1_8()) {
                this.itemStack = setUnbreakableNbt(this.itemStack, unbreakable);
                
                // 如果需要隐藏标签且物品元数据存在
                if (hide && this.itemStack.getItemMeta() != null) {
                    ItemMeta meta = this.itemStack.getItemMeta();
                    try {
                        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    } catch (Exception ignored) {
                        // 1.8可能不支持HIDE_UNBREAKABLE，忽略错误
                    }
                    this.itemStack.setItemMeta(meta);
                }
                return this;
            }
            
            // 对于更高版本，尝试使用原生API
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                try {
                    // 尝试使用反射调用setUnbreakable方法（1.9-1.10）
                    Method setUnbreakableMethod = ItemMeta.class.getDeclaredMethod("setUnbreakable", boolean.class);
                    setUnbreakableMethod.invoke(itemMeta, unbreakable);
                    
                    if (hide) {
                        try {
                            itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                        } catch (Exception ignored) {
                            // 某些版本可能不支持此标志
                        }
                    }
                    
                    itemStack.setItemMeta(itemMeta);
                } catch (Exception e) {
                    // 如果反射失败，尝试使用NBT方法
                    this.itemStack = setUnbreakableNbt(this.itemStack, unbreakable);
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("设置物品不可破坏失败: " + e.getMessage());
        }
        
        return this;
    }
    
    /**
     * 使用NBT标签设置物品为不可破坏(适用于1.8)
     * @param item 需要设置的物品
     * @param unbreakable 是否不可破坏
     * @return 设置后的物品
     */
    private ItemStack setUnbreakableNbt(ItemStack item, boolean unbreakable) {
        if (item == null) {
            return null;
        }
        
        // 如果反射未初始化且需要设置为不可破坏，尝试初始化反射
        if (!reflectionInitialized && unbreakable) {
            try {
                String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                
                craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
                nmsItemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
                nbtTagCompoundClass = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
                
                asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
                getTagMethod = nmsItemStackClass.getMethod("getTag");
                setBooleanMethod = nbtTagCompoundClass.getMethod("setBoolean", String.class, boolean.class);
                setTagMethod = nmsItemStackClass.getMethod("setTag", nbtTagCompoundClass);
                asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
                
                reflectionInitialized = true;
            } catch (Exception e) {
                LoggerUtil.warn("无法初始化NMS反射: " + e.getMessage());
                return item;
            }
        }
        
        // 如果反射初始化失败或不需要设置为不可破坏，直接返回原物品
        if (!reflectionInitialized || !unbreakable) {
            return item;
        }
        
        try {
            // 使用缓存的反射对象
            Object nmsItem = asNMSCopyMethod.invoke(null, item);
            Object tag = getTagMethod.invoke(nmsItem);
            
            // 如果没有标签就创建一个
            if (tag == null) {
                tag = nbtTagCompoundClass.getDeclaredConstructor().newInstance();
            }
            
            // 设置Unbreakable标签
            setBooleanMethod.invoke(tag, "Unbreakable", true);
            
            // 将标签设置回物品
            setTagMethod.invoke(nmsItem, tag);
            
            // 转换回Bukkit物品
            return (ItemStack) asBukkitCopyMethod.invoke(null, nmsItem);
        } catch (Exception e) {
            LoggerUtil.warn("使用NBT设置不可破坏失败: " + e.getMessage());
            return item;
        }
    }

    /**
     * 设置药水效果
     * @param potionEffect 药水效果
     * @return 构建器实例
     */
    public ItemBuilder setPotionData(PotionEffect potionEffect) {
        if (!(itemStack.getItemMeta() instanceof PotionMeta potionMeta)) {
            return this;
        }

        potionMeta.addCustomEffect(potionEffect, true);
        itemStack.setItemMeta(potionMeta);
        return this;
    }

    /**
     * 设置皮革装备颜色
     * @param color 颜色
     * @return 构建器实例
     */
    public ItemBuilder setColor(Color color) {
        if (!(itemStack.getItemMeta() instanceof LeatherArmorMeta leatherArmorMeta)) {
            return this;
        }

        leatherArmorMeta.setColor(color);
        itemStack.setItemMeta(leatherArmorMeta);
        return this;
    }

    /**
     * 设置羊毛颜色
     * @param dyeColor 染料颜色
     * @return 构建器实例
     */
    public ItemBuilder setWoolColor(DyeColor dyeColor) {
        this.itemStack = XMaterial.matchXMaterial(dyeColor.toString() + "_WOOL")
                // 找不到颜色名则用白色
                .orElse(XMaterial.WHITE_WOOL)
                .parseItem();
        return this;
    }

    /**
     * 设置玻璃颜色
     * @param dyeColor 染料颜色
     * @return 构建器实例
     */
    public ItemBuilder setGlassColor(DyeColor dyeColor) {
        this.itemStack = XMaterial.matchXMaterial(dyeColor.toString() + "_STAINED_GLASS")
                // 找不到颜色名则用白色
                .orElse(XMaterial.GLASS)
                .parseItem();
        return this;
    }

    /**
     * 设置物品数量
     * @param amount 数量
     * @return 构建器实例
     */
    public ItemBuilder setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * 设置物品耐久度/数据值
     * @param durability 耐久度/数据值
     * @return 构建器实例
     */
    public ItemBuilder setDurability(int durability) {
        // 如果是玩家头颅且数据值已经是3，不做修改
        if (isPlayerSkull(this.itemStack.getType()) && getDurabilityCompat(this.itemStack) == 3) {
            return this;
        }
        
        setDurabilityCompat(this.itemStack, (short) durability);
        return this;
    }
    
    /**
     * 检查是否是玩家头颅
     * @param material 材质
     * @return 是否是玩家头颅
     */
    private boolean isPlayerSkull(Material material) {
        return XMaterial.PLAYER_HEAD.get() == material;
    }
    
    /**
     * 兼容性方法：获取物品耐久度
     * @param item 物品
     * @return 耐久度
     */
    @SuppressWarnings("deprecation")
    private short getDurabilityCompat(ItemStack item) {
        if (!VersionUtil.isLessThan1_13()) {
            try {
                // 1.13+使用Damageable接口
                if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable) {
                    return (short) ((org.bukkit.inventory.meta.Damageable) item.getItemMeta()).getDamage();
                }
                return item.getDurability();
            } catch (Exception e) {
                return 0;
            }
        } else {
            // 1.8-1.12直接使用getDurability
            return item.getDurability();
        }
    }
    
    /**
     * 兼容性方法：设置物品耐久度/数据值
     * @param item 物品
     * @param durability 耐久度/数据值
     */
    @SuppressWarnings("deprecation")
    private void setDurabilityCompat(ItemStack item, short durability) {
        if (!VersionUtil.isLessThan1_13()) {
            // 1.13+尝试使用Damageable接口
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof org.bukkit.inventory.meta.Damageable) {
                ((org.bukkit.inventory.meta.Damageable) meta).setDamage(durability);
                item.setItemMeta(meta);
                return;
            }
        }
        
        // 旧版本或回退方案
        try {
            item.setDurability(durability);
        } catch (Exception e) {
            // 忽略异常
        }
    }

    /**
     * 设置物品显示名称
     * @param displayName 显示名称
     * @return 构建器实例
     */
    public ItemBuilder setDisplayName(String displayName) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(displayName);
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    /**
     * 设置物品Lore
     * @param lore Lore列表
     * @return 构建器实例
     */
    public ItemBuilder setLores(List<String> lore) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    /**
     * 设置物品Lore
     * @param strings Lore字符串数组
     * @return 构建器实例
     */
    public ItemBuilder setLores(String... strings) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setLore(Arrays.asList(strings));
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    /**
     * 添加单行Lore
     * @param string Lore行
     * @return 构建器实例
     */
    public ItemBuilder addLore(String string) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta == null) {
            return this;
        }
        
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        
        lore.add(string);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * 添加多行Lore
     * @param strings Lore行数组
     * @return 构建器实例
     */
    public ItemBuilder addLores(String... strings) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta == null) {
            return this;
        }
        
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        
        lore.addAll(Arrays.asList(strings));
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * 添加物品标记
     * @param itemFlag 物品标记
     * @return 构建器实例
     */
    public ItemBuilder addItemFlag(ItemFlag itemFlag) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.addItemFlags(itemFlag);
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    /**
     * 添加多个物品标记
     * @param itemFlags 物品标记数组
     * @return 构建器实例
     */
    public ItemBuilder addItemFlag(ItemFlag... itemFlags) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.addItemFlags(itemFlags);
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    /**
     * 移除物品标记
     * @param itemFlag 物品标记
     * @return 构建器实例
     */
    public ItemBuilder removeItemFlag(ItemFlag itemFlag) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.removeItemFlags(itemFlag);
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    /**
     * 添加附魔
     * @param enchantment 附魔类型
     * @param level 附魔等级
     * @return 构建器实例
     */
    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    @SuppressWarnings("all")
    public ItemBuilder setSpawnEggType(EntityType type) {
        XMaterial x = XMaterial.matchXMaterial(type.name().toUpperCase() + "_SPAWN_EGG").orElse(XMaterial.AIR);
        if (VersionUtil.isLessThan1_13()) {
            if ("MONSTER_EGG".equals(itemStack.getType().name()))
                itemStack.setDurability(x.getData());
        }
        else {
            itemStack.setType(x.get());
        }
        return this;
    }

    /**
     * 获取构建的物品
     * @return 完成的物品
     */
    public ItemStack getItem() {
        return this.itemStack.clone();
    }
}