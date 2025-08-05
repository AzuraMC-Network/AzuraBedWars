package cc.azuramc.bedwars.tablist;

import lombok.Getter;

import java.util.List;

/**
 * Header和Footer管理类
 * 负责管理Header和Footer内容
 *
 * @author an5w1r@163.com
 */
public class HeaderFooterManager {

    @Getter
    private String header = "";
    @Getter
    private String footer = "";

    /**
     * 设置Header内容
     */
    public void setHeader(String header) {
        this.header = header != null ? header : "";
    }

    /**
     * 设置Footer内容
     */
    public void setFooter(String footer) {
        this.footer = footer != null ? footer : "";
    }

    /**
     * 设置Header内容（支持列表）
     */
    public void setHeader(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            this.header = "";
        } else {
            this.header = String.join("\n", lines);
        }
    }

    /**
     * 设置Footer内容（支持列表）
     */
    public void setFooter(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            this.footer = "";
        } else {
            this.footer = String.join("\n", lines);
        }
    }

    /**
     * 清除Header和Footer
     */
    public void clear() {
        this.header = "";
        this.footer = "";
    }

    /**
     * 检查Header是否为空
     */
    public boolean isHeaderEmpty() {
        return header == null || header.isEmpty();
    }

    /**
     * 检查Footer是否为空
     */
    public boolean isFooterEmpty() {
        return footer == null || footer.isEmpty();
    }
}
