package cc.azuramc.bedwars.upgrade.upgrade;

import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.task.generator.GeneratorManager;
import cc.azuramc.bedwars.game.task.generator.PrivateResourceGenerator;
import com.cryptomorin.xseries.XMaterial;

/**
 * @author an5w1r@163.com
 */
public class UpgradeManager {

    private final GameTeam gameTeam;

    private boolean hasSharpnessUpgrade;
    private int protectionUpgrade;
    private int magicMinerUpgrade;
    private int resourceFurnaceUpgrade;
    private boolean hasHealPoolUpgrade;
    private int fallingProtectionUpgrade;

    public UpgradeManager(GameTeam gameTeam) {
        this.gameTeam = gameTeam;

        this.hasSharpnessUpgrade = false;
        this.protectionUpgrade = 0;
        this.magicMinerUpgrade = 0;
        this.resourceFurnaceUpgrade = 0;
        this.hasHealPoolUpgrade = false;
        this.fallingProtectionUpgrade = 0;
    }

    /**
     * 设置锋利升级状态
     *
     * @param hasUpgrade 是否拥有升级
     */
    public synchronized void setSharpnessUpgrade(boolean hasUpgrade) {
        this.hasSharpnessUpgrade = hasUpgrade;
    }

    /**
     * 获取锋利升级状态
     *
     * @return 是否拥有升级
     */
    public synchronized boolean hasSharpnessUpgrade() {
        return this.hasSharpnessUpgrade;
    }

    /**
     * 获取保护升级等级
     *
     * @return 升级等级
     */
    public synchronized int getProtectionUpgrade() {
        return this.protectionUpgrade;
    }

    /**
     * 设置保护升级等级
     *
     * @param level 升级等级
     */
    public synchronized void setProtectionUpgrade(int level) {
        this.protectionUpgrade = level;
    }

    /**
     * 获取疯狂矿工升级等级
     *
     * @return 升级等级
     */
    public synchronized int getMagicMinerUpgrade() {
        return this.magicMinerUpgrade;
    }

    /**
     * 设置疯狂矿工升级等级
     *
     * @param level 升级等级
     */
    public synchronized void setMagicMinerUpgrade(int level) {
        this.magicMinerUpgrade = level;
    }

    /**
     * 获取资源熔炉升级等级
     *
     * @return 升级等级
     */
    public synchronized int getResourceFurnaceUpgrade() {
        return this.resourceFurnaceUpgrade;
    }

    /**
     * 设置资源熔炉升级等级
     *
     * @param level 升级等级
     */
    public synchronized void setResourceFurnaceUpgrade(int level) {
        if (level < 0 || level > 5) {
            return;
        }

        GeneratorManager generatorManager = gameTeam.getGameManager().getGeneratorManager();

        PrivateResourceGenerator ironGenerator = generatorManager.getPrivateResourceGenerator("铁锭" + gameTeam.getName());
        PrivateResourceGenerator goldGenerator = generatorManager.getPrivateResourceGenerator("金锭" + gameTeam.getName());

        switch (level) {
            case 1:
                this.resourceFurnaceUpgrade = 1;
                ironGenerator.setInterval(20L * 1);
                ironGenerator.setMaxStack(48);

                goldGenerator.setInterval(20L * 3);
                goldGenerator.setMaxStack(8);
                break;
            case 2:
                this.resourceFurnaceUpgrade = 2;
                ironGenerator.setInterval((long) (20L * 0.8));
                ironGenerator.setMaxStack(72);

                goldGenerator.setInterval((long) (20L * 2.4));
                goldGenerator.setMaxStack(12);
                break;
            case 3:
                this.resourceFurnaceUpgrade = 3;
                ironGenerator.setInterval((long) (20L * 0.6));
                ironGenerator.setMaxStack(96);
                goldGenerator.setInterval(20L * 2);
                goldGenerator.setMaxStack(16);

                PrivateResourceGenerator emerald = null;
                if (XMaterial.EMERALD.get() != null) {
                    emerald = new PrivateResourceGenerator(
                            gameTeam.getGameManager(),
                            "绿宝石" + gameTeam.getName(),
                            gameTeam.getResourceDropLocation(),
                            XMaterial.EMERALD.get(),
                            2
                    );
                }
                generatorManager.addPrivateResourceTask(emerald, 20L * 60);
                break;
            case 4:
                this.resourceFurnaceUpgrade = 4;
                ironGenerator.setInterval((long) (20L * 0.4));
                ironGenerator.setMaxStack(128);
                goldGenerator.setInterval((long) (20L * 1.6));
                goldGenerator.setMaxStack(24);

                generatorManager.getPrivateResourceGenerator("绿宝石" + gameTeam.getName()).setInterval(20L * 30);
                break;
        }
    }

    /**
     * 设置治愈池升级状态
     *
     * @param hasUpgrade 是否拥有升级
     */
    public synchronized void setHealPoolUpgrade(boolean hasUpgrade) {
        this.hasHealPoolUpgrade = hasUpgrade;
    }

    /**
     * 获取治愈池升级状态
     *
     * @return 是否拥有升级
     */
    public synchronized boolean hasHealPoolUpgrade() {
        return this.hasHealPoolUpgrade;
    }

    /**
     * 取摔落保护升级等级
     *
     * @return 升级等级
     */
    public synchronized int getFallingProtectionUpgrade() {
        return this.fallingProtectionUpgrade;
    }

    /**
     * 原子性设置摔落保护升级等级
     *
     * @param level 升级等级
     */
    public synchronized void setFallingProtectionUpgrade(int level) {
        this.fallingProtectionUpgrade = level;
    }

    /**
     * 重置所有升级状态
     */
    public synchronized void resetAllUpgrades() {
        this.hasSharpnessUpgrade = false;
        this.protectionUpgrade = 0;
        this.magicMinerUpgrade = 0;
        this.resourceFurnaceUpgrade = 0;
        this.hasHealPoolUpgrade = false;
        this.fallingProtectionUpgrade = 0;
    }
}
