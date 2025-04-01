package cc.azuramc.bedwars.guis;

import cc.azuramc.bedwars.utils.gui.CustomGUI;
import cc.azuramc.bedwars.utils.gui.GUIAction;
import cc.azuramc.bedwars.utils.gui.NewGUIAction;
import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import cc.azuramc.bedwars.database.PlayerData;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.shop.ItemShopManager;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.data.DefaultShop;
import cc.azuramc.bedwars.shop.type.ColorType;
import cc.azuramc.bedwars.shop.type.ItemType;
import cc.azuramc.bedwars.shop.type.PriceCost;
import cc.azuramc.bedwars.types.ArmorType;
import cc.azuramc.bedwars.types.ModeType;
import cc.azuramc.bedwars.types.ToolType;
import cc.azuramc.bedwars.compat.material.MaterialUtil;
import cc.azuramc.bedwars.compat.enchantment.EnchantmentUtil;
import cc.azuramc.bedwars.compat.sound.SoundUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemShopGUI extends CustomGUI {
    private final Integer[] slots = new Integer[]{19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    public ItemShopGUI(Player player, int slot, Game game) {
        super(player, "§8道具商店 - " + ChatColor.stripColor(ItemShopManager.getShops().get(slot).getMainShopItem().getDisplayName()), 54);
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        PlayerData playerData = gamePlayer.getPlayerData();

        int i = 0;
        for (ShopData shopData : ItemShopManager.getShops()) {
            if (i > 9) {
                continue;
            }

            int finalI = i;
            setItem(i, new ItemBuilderUtil().setItemStack(shopData.getMainShopItem().getItemStack().clone()).setDisplayName(shopData.getMainShopItem().getDisplayName()).getItem(), new GUIAction(0, () -> {
                if (finalI != slot) new ItemShopGUI(player, finalI, game).open();
            }, false));
            ++i;
        }

        for (int i1 = 9; i1 < 18; i1++) {
            if (i1 == (slot + 9)) {
                setItem(i1, MaterialUtil.getStainedGlassPane(5), new GUIAction(0, () -> {
                }, false));
                continue;
            }

            setItem(i1, MaterialUtil.getStainedGlassPane(7), new GUIAction(0, () -> {
            }, false));
        }

        int j = -1;
        ShopData shopData = ItemShopManager.getShops().get(slot);
        if (shopData instanceof DefaultShop) {
            for (String s : playerData.getShopSort()) {
                j++;
                String[] strings = !s.equals("AIR") ? s.split("#") : null;

                ItemType itemType = null;
                if (strings != null && strings.length == 2) {
                    for (ShopData shopData1 : ItemShopManager.getShops()) {
                        if (shopData1.getClass().getSimpleName().equals(strings[0])) {
                            itemType = shopData1.getShopItems().get(Integer.parseInt(strings[1]) - 1);
                        }
                    }
                }

                if (strings == null || itemType == null) {
                    setItem(slots[j], new ItemBuilderUtil().setItemStack(MaterialUtil.getStainedGlassPane(14)).setDisplayName("§c空闲的槽位").setLores("§7这是一个快捷购买槽位!§bShift+左键", "§7将任意物品放到这里~").getItem(), new NewGUIAction(0, event -> {
                        if (!event.isShiftClick()) return;

                        player.sendMessage("§c这是个空的槽位!请使用Shift+左键添加物品到这里~");
                    }, false));
                    continue;
                }

                setItem(gamePlayer, slot, slots[j], game, itemType, -1, Arrays.asList("§7Shift+左键从快捷购买中移除", " "));
            }
            return;
        }

        for (ItemType itemType : shopData.getShopItems()) {
            j++;
            setItem(gamePlayer, slot, slots[j], game, itemType, j, null);
        }
    }

    public void setItem(GamePlayer gamePlayer, int slot, int size, Game game, ItemType itemType, int itemSlot, List<String> moreLore) {
        Player player = gamePlayer.getPlayer();
        PlayerData playerData = gamePlayer.getPlayerData();
        ModeType modeType = playerData.getModeType();

        ItemBuilderUtil itemBuilderUtil = new ItemBuilderUtil();
        itemBuilderUtil.setItemStack(itemType.getItemStack().clone());
        if (itemType.getColorType() == ColorType.PICKAXE) {
            switch (gamePlayer.getPickaxeType()) {
                case WOOD:
                    itemBuilderUtil.setType(MaterialUtil.STONE_PICKAXE());
                    itemType.setPriceCost(new PriceCost(MaterialUtil.IRON_INGOT(), 20, 20));
                    break;
                case STONE:
                    itemBuilderUtil.setType(MaterialUtil.IRON_PICKAXE());
                    itemType.setPriceCost(new PriceCost(MaterialUtil.GOLD_INGOT(), 8, 24));
                    break;
                case IRON:
                case DIAMOND:
                    itemBuilderUtil.setType(MaterialUtil.DIAMOND_PICKAXE());
                    itemType.setPriceCost(new PriceCost(MaterialUtil.GOLD_INGOT(), 12, 36));
                    break;
                default:
                    itemBuilderUtil.setType(MaterialUtil.WOODEN_PICKAXE());
                    itemType.setPriceCost(new PriceCost(MaterialUtil.IRON_INGOT(), 10, 10));
                    break;
            }
        } else if (itemType.getColorType() == ColorType.AXE) {
            switch (gamePlayer.getAxeType()) {
                case WOOD:
                    itemBuilderUtil.setType(MaterialUtil.STONE_AXE());
                    itemType.setPriceCost(new PriceCost(MaterialUtil.IRON_INGOT(), 20, 20));
                    break;
                case STONE:
                    itemBuilderUtil.setType(MaterialUtil.IRON_AXE());
                    itemType.setPriceCost(new PriceCost(MaterialUtil.GOLD_INGOT(), 8, 24));
                    break;
                case IRON:
                case DIAMOND:
                    itemBuilderUtil.setType(MaterialUtil.DIAMOND_AXE());
                    itemType.setPriceCost(new PriceCost(MaterialUtil.GOLD_INGOT(), 12, 36));
                    break;
                default:
                    itemBuilderUtil.setType(MaterialUtil.WOODEN_AXE());
                    itemType.setPriceCost(new PriceCost(MaterialUtil.IRON_INGOT(), 10, 10));
                    break;
            }
        }

        List<String> lore = new ArrayList<>();
        lore.add("§7物品:");
        lore.add("§8•" + itemType.getDisplayName());
        lore.add(" ");
        if (moreLore != null && !moreLore.isEmpty()) lore.addAll(moreLore);
        
        Material priceMaterial = itemType.getPriceCost().getMaterial();
        String resourceName = "";
        if (MaterialUtil.IRON_INGOT().equals(priceMaterial)) {
            resourceName = "铁";
        } else if (MaterialUtil.GOLD_INGOT().equals(priceMaterial)) {
            resourceName = "金";
        } else if (MaterialUtil.EMERALD().equals(priceMaterial)) {
            resourceName = "绿宝石";
        }
        
        lore.add(modeType == ModeType.EXPERIENCE ? "§7花费: §3§l" + itemType.getPriceCost().getXp() + "级" : "§7花费: §3§l" + itemType.getPriceCost().getAmount() + " " + resourceName);

        super.setItem(size, itemBuilderUtil.setDisplayName("§c" + itemType.getDisplayName()).setLores(lore).getItem(), new NewGUIAction(0, event -> {
            if (event.isShiftClick()) {
                if (slot == 0) {
                    int slot1 = Arrays.asList(slots).indexOf(size);
                    if (slot1 == -1) return;

                    playerData.getShopSort()[slot1] = "AIR";
                    playerData.saveShops();
                    new ItemShopGUI(player, slot, game).open();
                    return;
                }

                new DIYShopGUI(game, gamePlayer, itemBuilderUtil.getItem().clone(), ItemShopManager.getShops().get(slot).getClass().getSimpleName() + "#" + (itemSlot + 1)).open();
                return;
            }

            if (itemType.getColorType() == ColorType.PICKAXE && gamePlayer.getPickaxeType() == ToolType.DIAMOND) {
                return;
            }

            if (itemType.getColorType() == ColorType.AXE && gamePlayer.getAxeType() == ToolType.DIAMOND) {
                return;
            }

            Material itemMaterial = itemBuilderUtil.getItem().getType();
            if (MaterialUtil.SHEARS().equals(itemMaterial) && gamePlayer.isShear()) {
                return;
            }

            if (modeType == ModeType.DEFAULT) {
                int k = 0;
                int i1 = player.getInventory().getContents().length;
                ItemStack[] itemStacks = player.getInventory().getContents();
                for (int j1 = 0; j1 < i1; ++j1) {
                    ItemStack itemStack1 = itemStacks[j1];
                    if (itemStack1 != null && itemStack1.getType().equals(itemType.getPriceCost().getMaterial())) {
                        k += itemStack1.getAmount();
                    }
                }

                if (k >= itemType.getPriceCost().getAmount()) {
                    int amount = itemType.getPriceCost().getAmount();
                    i1 = player.getInventory().getContents().length;
                    itemStacks = player.getInventory().getContents();
                    for (int j1 = 0; j1 < i1; ++j1) {
                        ItemStack itemStack1 = itemStacks[j1];
                        if (itemStack1 != null && itemStack1.getType().equals(itemType.getPriceCost().getMaterial()) && amount > 0) {
                            if (itemStack1.getAmount() >= amount) {
                                itemStack1.setAmount(itemStack1.getAmount() - amount);
                                amount = 0;
                            } else if (itemStack1.getAmount() < amount) {
                                amount -= itemStack1.getAmount();
                                itemStack1.setAmount(0);
                            }

                            player.getInventory().setItem(j1, itemStack1);
                        }
                    }

                    player.playSound(player.getLocation(), SoundUtil.get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"), 1f, 1f);
                } else {
                    player.playSound(player.getLocation(), SoundUtil.get("ENTITY_ENDERMAN_TELEPORT", "ENTITY_ENDERMAN_TELEPORT"), 30.0F, 1.0F);
                    player.sendMessage("§c没有足够资源购买！");
                    return;
                }
            } else {
                if (player.getLevel() >= itemType.getPriceCost().getXp()) {
                    player.setLevel(player.getLevel() - itemType.getPriceCost().getXp());
                    player.playSound(player.getLocation(), SoundUtil.get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"), 1f, 1f);
                } else {
                    player.playSound(player.getLocation(), SoundUtil.get("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"), 30.0F, 1.0F);
                    player.sendMessage("§c没有足够资源购买！");
                    return;
                }
            }

            Material material = itemBuilderUtil.getItem().getType();
            if (MaterialUtil.CHAINMAIL_BOOTS().equals(material)) {
                gamePlayer.setArmorType(ArmorType.CHAINMAIL);
                gamePlayer.giveArmor();
                player.updateInventory();
                new ItemShopGUI(player, slot, game).open();
                return;
            } else if (MaterialUtil.IRON_BOOTS().equals(material)) {
                gamePlayer.setArmorType(ArmorType.IRON);
                gamePlayer.giveArmor();
                player.updateInventory();
                new ItemShopGUI(player, slot, game).open();
                return;
            } else if (MaterialUtil.DIAMOND_BOOTS().equals(material)) {
                gamePlayer.setArmorType(ArmorType.DIAMOND);
                gamePlayer.giveArmor();
                player.updateInventory();
                new ItemShopGUI(player, slot, game).open();
                return;
            } else if (MaterialUtil.WOODEN_PICKAXE().equals(material)) {
                gamePlayer.setPickaxeType(ToolType.WOOD);
                gamePlayer.givePickaxe(false);
                new ItemShopGUI(player, slot, game).open();
                return;
            } else if (MaterialUtil.STONE_PICKAXE().equals(material)) {
                gamePlayer.setPickaxeType(ToolType.STONE);
                gamePlayer.givePickaxe(true);
                new ItemShopGUI(player, slot, game).open();
                return;
            } else if (MaterialUtil.IRON_PICKAXE().equals(material)) {
                gamePlayer.setPickaxeType(ToolType.IRON);
                gamePlayer.givePickaxe(true);
                new ItemShopGUI(player, slot, game).open();
                return;
            } else if (MaterialUtil.DIAMOND_PICKAXE().equals(material)) {
                gamePlayer.setPickaxeType(ToolType.DIAMOND);
                gamePlayer.givePickaxe(true);
                new ItemShopGUI(player, slot, game).open();
                return;
            } else if (MaterialUtil.WOODEN_AXE().equals(material)) {
                gamePlayer.setAxeType(ToolType.WOOD);
                gamePlayer.giveAxe(false);
                new ItemShopGUI(player, slot, game).open();
                return;
            } else if (MaterialUtil.STONE_AXE().equals(material)) {
                gamePlayer.setAxeType(ToolType.STONE);
                gamePlayer.giveAxe(true);
                new ItemShopGUI(player, slot, game).open();
                return;
            } else if (MaterialUtil.IRON_AXE().equals(material)) {
                gamePlayer.setAxeType(ToolType.IRON);
                gamePlayer.giveAxe(true);
                new ItemShopGUI(player, slot, game).open();
                return;
            } else if (MaterialUtil.DIAMOND_AXE().equals(material)) {
                gamePlayer.setAxeType(ToolType.DIAMOND);
                gamePlayer.giveAxe(true);
                new ItemShopGUI(player, slot, game).open();
                return;
            } else if (MaterialUtil.SHEARS().equals(material)) {
                gamePlayer.setShear(true);
                gamePlayer.giveShear();
                new ItemShopGUI(player, slot, game).open();
                return;
            }

            ItemBuilderUtil itemBuilderUtil1 = new ItemBuilderUtil().setItemStack(itemType.getItemStack().clone());
            String itemTypeName = itemType.getItemStack().getType().toString();
            if (itemTypeName.endsWith("_SWORD") || itemTypeName.endsWith("SWORD")) {
                player.getInventory().remove(MaterialUtil.WOODEN_SWORD());

                if (gamePlayer.getGameTeam().isSharpenedSwords()) {
                    Enchantment sharpness = EnchantmentUtil.DAMAGE_ALL();
                    if (sharpness != null) {
                        itemBuilderUtil1.addEnchant(sharpness, 1);
                    }
                }
            }

            if (itemType.getColorType() == ColorType.COLOR) {
                itemBuilderUtil1.setDurability(MaterialUtil.getWoolData(gamePlayer.getGameTeam().getDyeColor()));
            }

            player.getInventory().addItem(itemBuilderUtil1.getItem());
        }, false));
    }
}
