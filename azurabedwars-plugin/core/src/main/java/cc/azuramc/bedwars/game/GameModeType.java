package cc.azuramc.bedwars.game;

/**
 * @author an5w1r@163.com
 */

public enum GameModeType {
    /**
     * 默认模式 捡起的资源不进行转换
     */
    DEFAULT {
        @Override
        public String toString() {
            return "DEFAULT";
        }
    },
    /**
     * 经验模式 捡起的资源自动兑换为经验
     */
    EXPERIENCE {
        @Override
        public String toString() {
            return "EXPERIENCE";
        }
    }
}
