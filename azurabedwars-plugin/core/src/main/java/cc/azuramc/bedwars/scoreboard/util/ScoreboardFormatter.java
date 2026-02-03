package cc.azuramc.bedwars.scoreboard.util;

import cc.azuramc.bedwars.util.MessageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
public class ScoreboardFormatter {

    private final Map<String, String> variables = new HashMap<>();

    public ScoreboardFormatter set(String key, String value) {
        variables.put(key, value != null ? value : "");
        return this;
    }

    public ScoreboardFormatter set(String key, int value) {
        variables.put(key, String.valueOf(value));
        return this;
    }

    public String format(String template) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return MessageUtil.color(result);
    }

    public List<String> formatLines(List<String> templates) {
        List<String> result = new ArrayList<>();
        for (String template : templates) {
            result.add(format(template));
        }
        return result;
    }

    /**
     * 格式化带特殊标记的行列表
     * 支持 {teams} 等特殊标记被替换为多行内容
     *
     * @param templates 模板行列表
     * @param teamLines 队伍行列表（用于替换 {teams}）
     * @return 格式化后的行列表
     */
    public List<String> formatLinesWithTeams(List<String> templates, List<String> teamLines) {
        List<String> result = new ArrayList<>();
        for (String template : templates) {
            if (template.trim().equals("{teams}")) {
                result.addAll(teamLines);
            } else {
                result.add(format(template));
            }
        }
        return result;
    }

    public static ScoreboardFormatter create() {
        return new ScoreboardFormatter();
    }
}
