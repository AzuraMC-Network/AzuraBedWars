package cc.azuramc.bedwars.guis;

import cc.azuramc.bedwars.utils.gui.CustomGUI;
import cc.azuramc.bedwars.utils.gui.GUIAction;
import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.types.ModeType;
import cc.azuramc.bedwars.compat.enchantment.EnchantmentUtil;
import cc.azuramc.bedwars.compat.sound.SoundUtil;
import cc.azuramc.bedwars.compat.material.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class TeamShopGUI extends CustomGUI {
    public TeamShopGUI(Player player, Game game) {
        super(player, "§8团队升级", 45);
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer == null) {
            return;
        }

        ModeType modeType = gamePlayer.getPlayerData().getModeType();

        if (!gamePlayer.getGameTeam().isSharpenedSwords()) {
            setItem(11, new ItemBuilderUtil().setType(Material.IRON_SWORD).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setDisplayName("§a磨刀石").setLores("§7你放所有成员的剑将获得锋利I附魔！", " ", "§7花费: §3§l" + (modeType == ModeType.DEFAULT ? "4 钻石" : "400级")).getItem(), new GUIAction(0, () -> {
                GameTeam gameTeam = gamePlayer.getGameTeam();

                if (modeType == ModeType.DEFAULT) {
                    int k = 0;
                    int i1 = player.getInventory().getContents().length;
                    ItemStack[] itemStacks = player.getInventory().getContents();
                    for (int j1 = 0; j1 < i1; ++j1) {
                        ItemStack itemStack1 = itemStacks[j1];
                        if (itemStack1 != null && itemStack1.getType().equals(Material.DIAMOND)) {
                            k += itemStack1.getAmount();
                        }
                    }

                    if (k >= 4) {
                        int amount = 4;
                        i1 = player.getInventory().getContents().length;
                        itemStacks = player.getInventory().getContents();
                        for (int j1 = 0; j1 < i1; ++j1) {
                            ItemStack itemStack1 = itemStacks[j1];
                            if (itemStack1 != null && itemStack1.getType().equals(Material.DIAMOND) && amount > 0) {
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
                        player.playSound(player.getLocation(), SoundUtil.get("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"), 30.0F, 1.0F);
                        player.sendMessage("§c没有足够资源购买！");
                        return;
                    }
                } else {
                    if (player.getLevel() >= 400) {
                        player.setLevel(player.getLevel() - 400);
                        player.playSound(player.getLocation(), SoundUtil.get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"), 1f, 1f);
                    } else {
                        player.playSound(player.getLocation(), SoundUtil.get("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"), 30.0F, 1.0F);
                        player.sendMessage("§c没有足够资源购买！");
                        return;
                    }
                }

                gameTeam.setSharpenedSwords(true);
                new TeamShopGUI(player, game).open();

                for (GamePlayer gamePlayer1 : gameTeam.getAlivePlayers()) {
                    Player player1 = gamePlayer1.getPlayer();

                    for (int i = 0; i < player1.getInventory().getContents().length; i++) {
                        ItemStack itemStack1 = player1.getInventory().getContents()[i];
                        if (itemStack1 != null && itemStack1.getType().toString().endsWith("_SWORD")) {
                            itemStack1.addEnchantment(EnchantmentUtil.DAMAGE_ALL(), 1);
                        }
                    }
                }
            }, false));
        } else {
            setItem(11, new ItemBuilderUtil().setType(Material.IRON_SWORD).addEnchant(EnchantmentUtil.DIG_SPEED(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setDisplayName("§a磨刀石").setLores(" ", "§c满级").getItem(), new GUIAction(0, () -> {
            }, false));
        }

        Runnable runnable = () -> {
            GameTeam gameTeam = gamePlayer.getGameTeam();
            int money = 0;
            switch (gameTeam.getReinforcedArmor()) {
                case 0:
                    money = 2;
                    break;
                case 1:
                    money = 4;
                    break;
                case 2:
                    money = 8;
                    break;
                default:
                    return;
            }

            if (modeType == ModeType.DEFAULT) {
                int k = 0;
                int i1 = player.getInventory().getContents().length;
                ItemStack[] itemStacks = player.getInventory().getContents();
                for (int j1 = 0; j1 < i1; ++j1) {
                    ItemStack itemStack1 = itemStacks[j1];
                    if (itemStack1 != null && itemStack1.getType().equals(Material.DIAMOND)) {
                        k += itemStack1.getAmount();
                    }
                }

                if (k >= money) {
                    int amount = money;
                    i1 = player.getInventory().getContents().length;
                    itemStacks = player.getInventory().getContents();
                    for (int j1 = 0; j1 < i1; ++j1) {
                        ItemStack itemStack1 = itemStacks[j1];
                        if (itemStack1 != null && itemStack1.getType().equals(Material.DIAMOND) && amount > 0) {
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
                    player.playSound(player.getLocation(), SoundUtil.get("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"), 30.0F, 1.0F);
                    player.sendMessage("§c没有足够资源购买！");
                    return;
                }
            } else {
                if (player.getLevel() >= (money * 100)) {
                    player.setLevel(player.getLevel() - (money * 100));
                    player.playSound(player.getLocation(), SoundUtil.get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"), 1f, 1f);
                } else {
                    player.playSound(player.getLocation(), SoundUtil.get("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"), 30.0F, 1.0F);
                    player.sendMessage("§c没有足够资源购买！");
                    return;
                }
            }

            gameTeam.setReinforcedArmor(gameTeam.getReinforcedArmor() + 1);
            new TeamShopGUI(player, game).open();

            for (GamePlayer gamePlayer1 : gameTeam.getAlivePlayers()) {
                Player player1 = gamePlayer1.getPlayer();

                for (int i = 0; i < player1.getInventory().getArmorContents().length; i++) {
                    ItemStack itemStack1 = player1.getInventory().getArmorContents()[i];
                    itemStack1.addEnchantment(EnchantmentUtil.PROTECTION_ENVIRONMENTAL(), gamePlayer.getGameTeam().getReinforcedArmor());
                }
            }
        };

        switch (gamePlayer.getGameTeam().getReinforcedArmor()) {
            case 0:
                setItem(12, new ItemBuilderUtil().setType(MaterialUtil.IRON_CHESTPLATE()).setDisplayName("§a精制护甲").setLores("§7你放所有成员的盔甲将获得保护I附魔！", " ", "§7花费: §3§l" + (modeType == ModeType.DEFAULT ? "2 钻石" : "200级")).getItem(), new GUIAction(0, runnable, false));
                break;
            case 1:
                setItem(12, new ItemBuilderUtil().setType(MaterialUtil.IRON_CHESTPLATE()).setDisplayName("§a精制护甲").setLores("§7你放所有成员的盔甲将获得保护II附魔！", " ", "§7花费: §3§l" + (modeType == ModeType.DEFAULT ? "4 钻石" : "400级")).getItem(), new GUIAction(0, runnable, false));
                break;
            case 2:
                setItem(12, new ItemBuilderUtil().setType(MaterialUtil.IRON_CHESTPLATE()).setDisplayName("§a精制护甲").setLores("§7你放所有成员的盔甲将获得保护III附魔！", " ", "§7花费: §3§l" + (modeType == ModeType.DEFAULT ? "8 钻石" : "800级")).getItem(), new GUIAction(0, runnable, false));
                break;
            case 3:
                setItem(12, new ItemBuilderUtil().setType(MaterialUtil.IRON_CHESTPLATE()).setDisplayName("§a精制护甲").setLores(" ", "§c满级").addEnchant(EnchantmentUtil.DIG_SPEED(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), new GUIAction(0, () -> {
                }, false));
                break;
            default:
                break;
        }

        Runnable runnable1 = () -> {
            GameTeam gameTeam = gamePlayer.getGameTeam();
            int money = 0;
            switch (gameTeam.getManicMiner()) {
                case 0:
                    money = 2;
                    break;
                case 1:
                    money = 4;
                    break;
                default:
                    return;
            }

            if (modeType == ModeType.DEFAULT) {
                int k = 0;
                int i1 = player.getInventory().getContents().length;
                ItemStack[] itemStacks = player.getInventory().getContents();
                for (int j1 = 0; j1 < i1; ++j1) {
                    ItemStack itemStack1 = itemStacks[j1];
                    if (itemStack1 != null && itemStack1.getType().equals(Material.DIAMOND)) {
                        k += itemStack1.getAmount();
                    }
                }

                if (k >= money) {
                    int amount = money;
                    i1 = player.getInventory().getContents().length;
                    itemStacks = player.getInventory().getContents();
                    for (int j1 = 0; j1 < i1; ++j1) {
                        ItemStack itemStack1 = itemStacks[j1];
                        if (itemStack1 != null && itemStack1.getType().equals(Material.DIAMOND) && amount > 0) {
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
                    player.playSound(player.getLocation(), SoundUtil.get("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"), 30.0F, 1.0F);
                    player.sendMessage("§c没有足够资源购买！");
                    return;
                }
            } else {
                if (player.getLevel() >= (money * 100)) {
                    player.setLevel(player.getLevel() - (money * 100));
                    player.playSound(player.getLocation(), SoundUtil.get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"), 1f, 1f);
                } else {
                    player.playSound(player.getLocation(), SoundUtil.get("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"), 30.0F, 1.0F);
                    player.sendMessage("§c没有足够资源购买！");
                    return;
                }
            }

            gameTeam.setManicMiner(gameTeam.getManicMiner() + 1);
            new TeamShopGUI(player, game).open();
        };

        switch (gamePlayer.getGameTeam().getManicMiner()) {
            case 0:
                setItem(13, new ItemBuilderUtil().setType(MaterialUtil.GOLDEN_PICKAXE()).setDisplayName("§a疯狂矿工").setLores("§7你放所有成员将永久拥有急迫I效果！", " ", "§7花费: §3§l" + (modeType == ModeType.DEFAULT ? "2 钻石" : "200级")).getItem(), new GUIAction(0, runnable1, false));
                break;
            case 1:
                setItem(13, new ItemBuilderUtil().setType(MaterialUtil.GOLDEN_PICKAXE()).setDisplayName("§a疯狂矿工").setLores("§7你放所有成员将永久拥有急迫II效果！", " ", "§7花费: §3§l" + (modeType == ModeType.DEFAULT ? "4 钻石" : "400级")).getItem(), new GUIAction(0, runnable1, false));
                break;
            case 2:
                setItem(13, new ItemBuilderUtil().setType(MaterialUtil.GOLDEN_PICKAXE()).setDisplayName("§a疯狂矿工").setLores(" ", "§c满级").addEnchant(EnchantmentUtil.DIG_SPEED(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), new GUIAction(0, () -> {
                }, false));
                break;
            default:
                break;
        }

        if (!gamePlayer.getGameTeam().isMiner()) {
            setItem(14, new ItemBuilderUtil().setType(MaterialUtil.IRON_PICKAXE()).addEnchant(EnchantmentUtil.DIG_SPEED(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setDisplayName("§a挖掘疲劳陷阱").setLores("§7下一个进入基地的敌人,会获得储蓄10秒的挖掘疲劳效果！", " ", "§7花费: §3§l" + (modeType == ModeType.DEFAULT ? "2 钻石" : "200级")).getItem(), new GUIAction(0, () -> {
                GameTeam gameTeam = gamePlayer.getGameTeam();

                if (modeType == ModeType.DEFAULT) {
                    int k = 0;
                    int i1 = player.getInventory().getContents().length;
                    ItemStack[] itemStacks = player.getInventory().getContents();
                    for (int j1 = 0; j1 < i1; ++j1) {
                        ItemStack itemStack1 = itemStacks[j1];
                        if (itemStack1 != null && itemStack1.getType().equals(Material.DIAMOND)) {
                            k += itemStack1.getAmount();
                        }
                    }

                    if (k >= 2) {
                        int amount = 2;
                        i1 = player.getInventory().getContents().length;
                        itemStacks = player.getInventory().getContents();
                        for (int j1 = 0; j1 < i1; ++j1) {
                            ItemStack itemStack1 = itemStacks[j1];
                            if (itemStack1 != null && itemStack1.getType().equals(Material.DIAMOND) && amount > 0) {
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
                        player.playSound(player.getLocation(), SoundUtil.get("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"), 30.0F, 1.0F);
                        player.sendMessage("§c没有足够资源购买！");
                        return;
                    }
                } else {
                    if (player.getLevel() >= 200) {
                        player.setLevel(player.getLevel() - 200);
                        player.playSound(player.getLocation(), SoundUtil.get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"), 1f, 1f);
                    } else {
                        player.playSound(player.getLocation(), SoundUtil.get("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"), 30.0F, 1.0F);
                        player.sendMessage("§c没有足够资源购买！");
                        return;
                    }
                }

                gameTeam.setMiner(true);
                new TeamShopGUI(player, game).open();
            }, false));
        } else {
            setItem(14, new ItemBuilderUtil().setType(MaterialUtil.IRON_PICKAXE()).addEnchant(EnchantmentUtil.DIG_SPEED(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setDisplayName("§a挖掘疲劳陷阱").setLores(" ", "§c满级").getItem(), new GUIAction(0, () -> {
            }, false));
        }

        if (!gamePlayer.getGameTeam().isHealPool()) {
            setItem(15, new ItemBuilderUtil().setType(MaterialUtil.BEACON()).addEnchant(EnchantmentUtil.DIG_SPEED(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setDisplayName("§a治愈池").setLores("§7基地附近的队伍成员获得生命恢复效果", " ", "§7花费: §3§l" + (modeType == ModeType.DEFAULT ? "4 钻石" : "400级")).getItem(), new GUIAction(0, () -> {
                GameTeam gameTeam = gamePlayer.getGameTeam();

                if (modeType == ModeType.DEFAULT) {
                    int k = 0;
                    int i1 = player.getInventory().getContents().length;
                    ItemStack[] itemStacks = player.getInventory().getContents();
                    for (int j1 = 0; j1 < i1; ++j1) {
                        ItemStack itemStack1 = itemStacks[j1];
                        if (itemStack1 != null && itemStack1.getType().equals(Material.DIAMOND)) {
                            k += itemStack1.getAmount();
                        }
                    }

                    if (k >= 4) {
                        int amount = 4;
                        i1 = player.getInventory().getContents().length;
                        itemStacks = player.getInventory().getContents();
                        for (int j1 = 0; j1 < i1; ++j1) {
                            ItemStack itemStack1 = itemStacks[j1];
                            if (itemStack1 != null && itemStack1.getType().equals(Material.DIAMOND) && amount > 0) {
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
                        player.playSound(player.getLocation(), SoundUtil.get("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"), 30.0F, 1.0F);
                        player.sendMessage("§c没有足够资源购买！");
                        return;
                    }
                } else {
                    if (player.getLevel() >= 400) {
                        player.setLevel(player.getLevel() - 400);
                        player.playSound(player.getLocation(), SoundUtil.get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"), 1f, 1f);
                    } else {
                        player.playSound(player.getLocation(), SoundUtil.get("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"), 30.0F, 1.0F);
                        player.sendMessage("§c没有足够资源购买！");
                        return;
                    }
                }

                gameTeam.setHealPool(true);
                new TeamShopGUI(player, game).open();
            }, false));
        } else {
            setItem(15, new ItemBuilderUtil().setType(MaterialUtil.BEACON()).addEnchant(EnchantmentUtil.DIG_SPEED(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setDisplayName("§a治愈池").setLores(" ", "§c满级").getItem(), new GUIAction(0, () -> {
            }, false));
        }

        if (!gamePlayer.getGameTeam().isTrap()) {
            setItem(31, new ItemBuilderUtil().setType(MaterialUtil.TRIPWIRE_HOOK()).addEnchant(EnchantmentUtil.DIG_SPEED(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setDisplayName("§a这是个陷阱！").setLores("§7下一个来犯之敌将获得失明和缓慢效果！", " ", "§7花费: §3§l" + (modeType == ModeType.DEFAULT ? "1 钻石" : "100级")).getItem(), new GUIAction(0, () -> {
                GameTeam gameTeam = gamePlayer.getGameTeam();

                if (modeType == ModeType.DEFAULT) {
                    int k = 0;
                    int i1 = player.getInventory().getContents().length;
                    ItemStack[] itemStacks = player.getInventory().getContents();
                    for (int j1 = 0; j1 < i1; ++j1) {
                        ItemStack itemStack1 = itemStacks[j1];
                        if (itemStack1 != null && itemStack1.getType().equals(Material.DIAMOND)) {
                            k += itemStack1.getAmount();
                        }
                    }

                    if (k >= 1) {
                        int amount = 1;
                        i1 = player.getInventory().getContents().length;
                        itemStacks = player.getInventory().getContents();
                        for (int j1 = 0; j1 < i1; ++j1) {
                            ItemStack itemStack1 = itemStacks[j1];
                            if (itemStack1 != null && itemStack1.getType().equals(Material.DIAMOND) && amount > 0) {
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
                        player.playSound(player.getLocation(), SoundUtil.get("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"), 30.0F, 1.0F);
                        player.sendMessage("§c没有足够资源购买！");
                        return;
                    }
                } else {
                    if (player.getLevel() >= 100) {
                        player.setLevel(player.getLevel() - 100);
                        player.playSound(player.getLocation(), SoundUtil.get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"), 1f, 1f);
                    } else {
                        player.playSound(player.getLocation(), SoundUtil.get("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"), 30.0F, 1.0F);
                        player.sendMessage("§c没有足够资源购买！");
                        return;
                    }
                }

                gameTeam.setTrap(true);
                new TeamShopGUI(player, game).open();
            }, false));
        } else {
            setItem(31, new ItemBuilderUtil().setType(MaterialUtil.TRIPWIRE_HOOK()).addEnchant(EnchantmentUtil.DIG_SPEED(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setDisplayName("§a这是个陷阱！").setLores(" ", "§c满级").getItem(), new GUIAction(0, () -> {
            }, false));
        }

        for (int i = 18; i < 27; i++) {
            setItem(i, MaterialUtil.getStainedGlassPane(7), new GUIAction(0, () -> {
            }, false));
        }
    }
}
