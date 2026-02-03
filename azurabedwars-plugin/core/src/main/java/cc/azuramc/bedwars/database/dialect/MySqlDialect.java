package cc.azuramc.bedwars.database.dialect;

/**
 * @author an5w1r@163.com
 */
public final class MySqlDialect implements SqlDialect {

    public static final MySqlDialect INSTANCE = new MySqlDialect();

    private MySqlDialect() {
    }

    @Override
    public String getDialectName() {
        return "MySQL";
    }

    @Override
    public String wrapIdentifier(String name) {
        return "`" + name + "`";
    }

    @Override
    public String getIntType() {
        return "INT";
    }

    @Override
    public String getDoubleType() {
        return "DOUBLE";
    }

    @Override
    public String getVarcharType(int size) {
        return "VARCHAR(" + size + ")";
    }

    @Override
    public String getTextType() {
        return "TEXT";
    }

    @Override
    public String getTimestampType() {
        return "TIMESTAMP";
    }

    @Override
    public String getBooleanType() {
        return "TINYINT(1)";
    }

    @Override
    public String getPrimaryKeyAutoIncrement() {
        return "INT PRIMARY KEY AUTO_INCREMENT";
    }

    @Override
    public String getPrimaryKeyDefinition(String type) {
        return type + " PRIMARY KEY";
    }

    @Override
    public String getDefaultCurrentTimestamp() {
        return "DEFAULT CURRENT_TIMESTAMP";
    }

    @Override
    public String getOnUpdateCurrentTimestamp() {
        return "ON UPDATE CURRENT_TIMESTAMP";
    }

    @Override
    public String getTableOptions() {
        return "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
    }
}
