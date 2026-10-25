package cc.azuramc.bedwars.shop;

/**
 * @author an5w1r@163.com
 */
public enum ShopItemKind {
    ITEM,
    TEAM_COLORED,
    SWORD,
    ARMOR,
    PICKAXE,
    AXE,
    SHEARS;

    public boolean isEquipment() {
        return this == ARMOR || this == PICKAXE || this == AXE || this == SHEARS;
    }
}
