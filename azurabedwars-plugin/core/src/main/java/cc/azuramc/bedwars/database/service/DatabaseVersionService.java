package cc.azuramc.bedwars.database.service;

import cc.azuramc.bedwars.database.repository.IDatabaseVersionRepository;
import cc.azuramc.bedwars.util.LoggerUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库版本服务
 * 提供数据库版本管理的业务逻辑
 *
 * @author An5w1r@163.com
 */
public class DatabaseVersionService {

    /**
     * 当前数据库版本号
     */
    private static final int CURRENT_VERSION = 1;
    private final IDatabaseVersionRepository databaseVersionRepository;

    /**
     * 版本升级映射表
     */
    private final Map<Integer, Runnable> versionUpgrades = new HashMap<>();

    public DatabaseVersionService(IDatabaseVersionRepository databaseVersionRepository) {
        this.databaseVersionRepository = databaseVersionRepository;
        this.initVersionUpgrades();
        this.initializeDatabase();
    }

    /**
     * 初始化版本升级映射
     */
    private void initVersionUpgrades() {
        // versionUpgrades.put(1, () -> {
        //     LoggerUtil.info("执行数据库版本升级: 1 -> 2");
        // });

        // versionUpgrades.put(2, () -> {
        //     LoggerUtil.info("执行数据库版本升级: 2 -> 3");
        // });
    }

    /**
     * 初始化数据库版本管理
     */
    private void initializeDatabase() {
        try {
            // 创建版本表
            databaseVersionRepository.createTable();

            // 检查当前版本
            int currentVersion = databaseVersionRepository.getCurrentVersion();

            if (currentVersion == -1) {
                // 首次安装，插入初始版本
                databaseVersionRepository.insertVersion(CURRENT_VERSION);
                LoggerUtil.info("数据库版本管理初始化完成，当前版本: " + CURRENT_VERSION);
            } else if (currentVersion != CURRENT_VERSION) {
                // 需要升级
                performVersionUpgrade(currentVersion, CURRENT_VERSION);
            } else {
                LoggerUtil.info("数据库版本检查完成，当前版本: " + currentVersion);
            }

        } catch (SQLException e) {
            LoggerUtil.error("数据库版本管理初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 执行版本升级
     *
     * @param fromVersion 当前版本
     * @param toVersion   目标版本
     */
    private void performVersionUpgrade(int fromVersion, int toVersion) {
        try {
            LoggerUtil.info("开始数据库版本升级: " + fromVersion + " -> " + toVersion);

            // 执行升级逻辑
            Runnable upgradeTask = versionUpgrades.get(fromVersion);
            if (upgradeTask != null) {
                upgradeTask.run();
            }

            // 更新版本记录
            if (databaseVersionRepository.hasVersionRecord()) {
                databaseVersionRepository.updateVersion(toVersion);
            } else {
                databaseVersionRepository.insertVersion(toVersion);
            }

            LoggerUtil.info("数据库版本升级完成: " + fromVersion + " -> " + toVersion);

        } catch (SQLException e) {
            LoggerUtil.error("数据库版本升级失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取当前数据库版本
     *
     * @return 当前版本号
     */
    public int getCurrentVersion() {
        try {
            return databaseVersionRepository.getCurrentVersion();
        } catch (SQLException e) {
            LoggerUtil.error("获取数据库版本失败: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 检查是否需要升级
     *
     * @return 如果需要升级返回true
     */
    public boolean needsUpgrade() {
        int currentVersion = getCurrentVersion();
        return currentVersion != -1 && currentVersion != CURRENT_VERSION;
    }

    /**
     * 手动执行升级
     */
    public void manualUpgrade() {
        int currentVersion = getCurrentVersion();
        if (currentVersion != -1 && currentVersion != CURRENT_VERSION) {
            performVersionUpgrade(currentVersion, CURRENT_VERSION);
        } else {
            LoggerUtil.info("数据库版本已是最新，无需升级");
        }
    }
}
