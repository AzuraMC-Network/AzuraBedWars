package cc.azuramc.bedwars.utils;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Wool;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilderUtil {
    private ItemStack itemStack;
    private static final boolean NEW_VERSION;
    
    // 静态初始化块，判断是否是新版本
    static {
        boolean newVersion = false;
        try {
            // 1.13+版本存在Material.PLAYER_HEAD
            Material.valueOf("PLAYER_HEAD");
            newVersion = true;
        } catch (IllegalArgumentException e) {
            // 1.8-1.12版本
            newVersion = false;
        }
        NEW_VERSION = newVersion;
    }

    public ItemBuilderUtil() {
        itemStack = new ItemStack(Material.AIR);
    }

    /**
     * 获取玩家头颅的Material，兼容新旧版本
     * @return 玩家头颅的Material
     */
    private static Material getPlayerSkullMaterial() {
        if (NEW_VERSION) {
            return Material.valueOf("PLAYER_HEAD");
        } else {
            return Material.valueOf("SKULL_ITEM");
        }
    }

    public ItemBuilderUtil setType(Material material) {
        itemStack.setType(material);
        return this;
    }

    public ItemBuilderUtil setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    public ItemBuilderUtil setOwner(String owner) {
        Material skullMaterial = getPlayerSkullMaterial();
        itemStack.setType(skullMaterial);
        
        // 为旧版本设置数据值
        if (!NEW_VERSION) {
            setDurabilityCompat(itemStack, (short) 3);
        }

        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        
        try {
            // 尝试新版本API (1.13+)
            Method setOwningPlayer = SkullMeta.class.getMethod("setOwningPlayer", org.bukkit.OfflinePlayer.class);
            
            // 使用UUID而不是直接通过名称获取离线玩家
            org.bukkit.OfflinePlayer offlinePlayer = null;
            try {
                // 尝试通过UUID获取玩家
                java.util.UUID uuid = java.util.UUID.fromString(owner);
                offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(uuid);
            } catch (Exception ex) {
                // 如果不是UUID格式，则使用名称获取玩家
                offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(owner);
            }
            
            setOwningPlayer.invoke(skullMeta, offlinePlayer);
        } catch (Exception e) {
            try {
                // 回退到旧版本API (1.8-1.12)
                skullMeta.setOwner(owner);
            } catch (Exception ex) {
                // 两种方法都失败
            }
        }
        
        itemStack.setItemMeta(skullMeta);
        return this;
    }

    public ItemBuilderUtil setUnbreakable(boolean unbreakable, boolean hide) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        
        try {
            // 尝试使用新版本API (1.11+)
            Method setUnbreakable = ItemMeta.class.getMethod("setUnbreakable", boolean.class);
            setUnbreakable.invoke(itemMeta, unbreakable);
        } catch (Exception e) {
            try {
                // 回退到旧版本API (1.8-1.10)
                Method spigot = ItemMeta.class.getMethod("spigot");
                Object spigotMeta = spigot.invoke(itemMeta);
                
                Method setUnbreakableMethod = spigotMeta.getClass().getMethod("setUnbreakable", boolean.class);
                setUnbreakableMethod.invoke(spigotMeta, unbreakable);
            } catch (Exception ex) {
                // 两种方法都失败
            }
        }
        
        if (unbreakable && hide) {
            try {
                itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            } catch (Exception e) {
                // 某些版本可能不支持此标志
            }
        }
        
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilderUtil setPotionData(PotionEffect potionEffect) {
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        potionMeta.addCustomEffect(potionEffect, true);
        itemStack.setItemMeta(potionMeta);
        return this;
    }

    public ItemBuilderUtil setColor(Color paramColor) {
        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
        leatherArmorMeta.setColor(paramColor);
        itemStack.setItemMeta(leatherArmorMeta);
        return this;
    }

    public ItemBuilderUtil setWoolColor(DyeColor dyeColor) {
        if (NEW_VERSION) {
            // 1.13+版本使用具体的Material
            try {
                // 尝试用新的命名方式
                String woolName = dyeColor.toString() + "_WOOL";
                Material woolMaterial = Material.valueOf(woolName);
                itemStack = new ItemStack(woolMaterial);
            } catch (Exception e) {
                // 如果新的命名方式不可用，使用默认白色羊毛
                itemStack = new ItemStack(Material.valueOf("WHITE_WOOL"));
            }
        } else {
            // 1.8-1.12版本使用Wool类
            try {
                Wool wool = new Wool(dyeColor);
                itemStack = wool.toItemStack(1);
            } catch (Exception e) {
                try {
                    // 如果Wool类不可用，创建普通的羊毛并设置数据值
                    itemStack = new ItemStack(Material.valueOf("WOOL"));
                    
                    // 使用反射获取getWoolData方法，处理getWoolData弃用问题
                    Method getWoolDataMethod = DyeColor.class.getMethod("getWoolData");
                    byte woolData = (byte) getWoolDataMethod.invoke(dyeColor);
                    setDurabilityCompat(itemStack, woolData);
                } catch (Exception ex) {
                    // 最后的回退方法，使用原始索引值
                    itemStack = new ItemStack(Material.valueOf("WOOL"));
                    setDurabilityCompat(itemStack, (short) dyeColor.ordinal());
                }
            }
        }
        return this;
    }

    public ItemBuilderUtil setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilderUtil setDurability(int durability) {
        // 如果是玩家头颅且数据值已经是3，不做修改
        if ((isPlayerSkull(this.itemStack.getType())) && (getDurabilityCompat(this.itemStack) == 3)) {
            return this;
        }
        
        setDurabilityCompat(this.itemStack, (short) durability);
        return this;
    }
    
    // 检查是否是玩家头颅
    private boolean isPlayerSkull(Material material) {
        if (NEW_VERSION) {
            return material == Material.valueOf("PLAYER_HEAD");
        } else {
            return material == Material.valueOf("SKULL_ITEM");
        }
    }
    
    // 兼容性方法：获取物品耐久度
    private short getDurabilityCompat(ItemStack item) {
        if (NEW_VERSION) {
            try {
                // 尝试使用新版本API - Damageable接口
                if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable) {
                    return (short) ((org.bukkit.inventory.meta.Damageable) item.getItemMeta()).getDamage();
                }
                // 如果不是Damageable，尝试使用旧的方法，可能已弃用但仍有效
                return item.getDurability();
            } catch (Exception e) {
                return 0;
            }
        } else {
            // 1.8-1.12版本直接使用getDurability
            return item.getDurability();
        }
    }
    
    // 兼容性方法：设置物品耐久度/数据值
    private void setDurabilityCompat(ItemStack item, short durability) {
        if (NEW_VERSION) {
            try {
                // 尝试使用新版本API - Damageable接口
                ItemMeta meta = item.getItemMeta();
                if (meta instanceof org.bukkit.inventory.meta.Damageable) {
                    ((org.bukkit.inventory.meta.Damageable) meta).setDamage(durability);
                    item.setItemMeta(meta);
                    return;
                }
            } catch (Exception e) {
                // 继续尝试旧方法
            }
            
            try {
                // 旧方法但仍可能在新版本中工作
                item.setDurability(durability);
            } catch (Exception e) {
                // 忽略错误，新版本很多物品不再使用数据值
            }
        } else {
            // 1.8-1.12版本直接设置数据值
            item.setDurability(durability);
        }
    }

    public ItemBuilderUtil setDisplayName(String displayName) {
        ItemMeta localItemMeta = this.itemStack.getItemMeta();
        localItemMeta.setDisplayName(displayName);
        itemStack.setItemMeta(localItemMeta);
        return this;
    }

    public ItemBuilderUtil setLores(List<String> list) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.setLore(list);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilderUtil setLores(String... strings) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.setLore(Arrays.asList(strings));
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilderUtil addLore(String string) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        List<String> lore = itemMeta.getLore();
        if (lore == null) lore = new ArrayList<>();
        lore.add(string);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilderUtil addLores(String... strings) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        List<String> lore = itemMeta.getLore();
        if (lore == null) lore = new ArrayList<>();
        lore.addAll(Arrays.asList(strings));
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilderUtil addItemFlag(ItemFlag itemFlag) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.addItemFlags(itemFlag);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilderUtil addItemFlag(ItemFlag... itemFlags) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.addItemFlags(itemFlags);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilderUtil removeItemFlag(ItemFlag itemFlag) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.removeItemFlags(itemFlag);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilderUtil addEnchant(Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }
}