package com.z0cken.mc.core;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.UUID;

@SuppressWarnings("unused")
public enum Shadow {
    NAME(JDBCType.VARCHAR, 40), IP(JDBCType.VARCHAR, 15), SEEN(JDBCType.TIMESTAMP, 0), MARK(JDBCType.TINYINT, 0), TERMS(JDBCType.TINYINT, 1);

    private JDBCType type;
    private int arg;

    Shadow(JDBCType type, int arg) {
        this.type = type;
        this.arg = arg;
    }

    public Integer getInt(@Nonnull UUID uuid) throws SQLException {
        try(Connection connection = Database.MAIN.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT " + name().toLowerCase() + " FROM shadow WHERE uuid = ?;")) {
            pstmt.setString(1, uuid.toString());
            ResultSet resultSet = pstmt.executeQuery();
            if(!resultSet.next()) return null;
            int i = resultSet.getInt(1);
            resultSet.close();
            return i;
        }
    }

    public void setInt(@Nonnull UUID uuid, int i) {
        try (Connection connection = Database.MAIN.getConnection();
            Statement statement = connection.createStatement()){
            final String s = name().toLowerCase() + " = " + i;
            statement.executeUpdate("INSERT INTO shadow SET " + s + ", uuid = '" + uuid + "' ON DUPLICATE KEY UPDATE " + s + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getString(@Nonnull UUID uuid) throws SQLException {
        try(Connection connection = Database.MAIN.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT " + name().toLowerCase() + " FROM shadow WHERE uuid = ?;")) {
            pstmt.setString(1, uuid.toString());
            ResultSet resultSet = pstmt.executeQuery();
            if(!resultSet.next()) return null;
            String s = resultSet.getString(1);
            resultSet.close();
            return s;
        }
    }

    public void setString(@Nonnull UUID uuid, String string) {
        try (Connection connection = Database.MAIN.getConnection();
             Statement statement = connection.createStatement()) {
            final String s = name().toLowerCase() + " = '" + string + "'";
            statement.executeUpdate("INSERT INTO shadow SET " + s + ", uuid = '" + uuid + "' ON DUPLICATE KEY UPDATE " + s + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean getBoolean(@Nonnull UUID uuid) throws SQLException {
        try(Connection connection = Database.MAIN.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT " + name().toLowerCase() + " FROM shadow WHERE uuid = ?;")) {
            pstmt.setString(1, uuid.toString());
            ResultSet resultSet = pstmt.executeQuery();
            if(!resultSet.next()) return false;
            boolean b = resultSet.getBoolean(1);
            resultSet.close();
            return b;
        }
    }

    public void setBoolean(@Nonnull UUID uuid, boolean b) {
        try (Connection connection = Database.MAIN.getConnection();
             Statement statement = connection.createStatement()) {
            final String s = name().toLowerCase() + " = '" + (b ? 1 : 0) + "'";
            statement.executeUpdate("INSERT INTO shadow SET " + s + ", uuid = '" + uuid + "' ON DUPLICATE KEY UPDATE " + s + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Timestamp getTimestamp(@Nonnull UUID uuid) throws SQLException {
        try(Connection connection = Database.MAIN.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT " + name().toLowerCase() + " FROM shadow WHERE uuid = ?;")) {
            pstmt.setString(1, uuid.toString());
            ResultSet resultSet = pstmt.executeQuery();
            if (!resultSet.next()) return null;
            Timestamp timestamp = resultSet.getTimestamp(1);
            resultSet.close();
            return timestamp;
        }
    }

    public UUID getUUID(@Nonnull UUID uuid) throws SQLException {
        try(Connection connection = Database.MAIN.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT " + name().toLowerCase() + " FROM shadow WHERE uuid = ?;")) {
            pstmt.setString(1, uuid.toString());
            ResultSet resultSet = pstmt.executeQuery();
            if(!resultSet.next()) return null;
            UUID id = UUID.fromString(resultSet.getString(1));
            resultSet.close();
            return id;
        }
    }

    public static UUID getByName(@Nonnull String name) throws SQLException {
        ResultSet resultSet = null;
        try(Connection connection = Database.MAIN.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT uuid FROM shadow WHERE lower(name) = ?;")) {
            pstmt.setString(1, name.toLowerCase());
            resultSet = pstmt.executeQuery();
            if(!resultSet.next()) return null;
            UUID uuid = UUID.fromString(resultSet.getString(1));
            if(resultSet.next()) throw new RuntimeException("Multiple shadow records for '" + name.toLowerCase() + "'");
            return uuid;
        } finally {
            if(resultSet != null) resultSet.close();
        }
    }

    static void setupTables() {
        try (Connection connection = Database.MAIN.getConnection();
             Statement statement = connection.createStatement()){

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS shadow (uuid CHAR(36) PRIMARY KEY);");
            for(Shadow shadow : values()) {
                statement.executeUpdate("ALTER TABLE shadow ADD COLUMN IF NOT EXISTS " + shadow.name().toLowerCase() + " " + shadow.type.getName() + "(" + shadow.arg + ");");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
