package cc.azuramc.bedwars.shop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class QuickBuyTest {

    @Test
    void convertsLayoutKeysOfTheOldShopPages() {
        assertEquals("blocks.clay", QuickBuy.convertLegacy("BlockShopPage#3"));
        assertEquals("utility.water", QuickBuy.convertLegacy("DefaultShopPage#14"));
        assertEquals("tools.shears", QuickBuy.convertLegacy("ToolShopPage#3"));
        assertNull(QuickBuy.convertLegacy("BlockShopPage#11"));
        assertNull(QuickBuy.convertLegacy("blocks.wool"));
    }
}
