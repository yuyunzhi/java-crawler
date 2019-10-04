package com.github.yuyunzhi;

import java.sql.*;

public class JdbcCrawlerDao implements CrawlerDAO {
    private static final String USER_NAME = "root";
    private static final String PASS_WORD = "root";
    private final Connection connection;

    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:file:/Users/yuyunzhi/Desktop/java-crawler/crawler", USER_NAME, PASS_WORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private String getNextLinkFromDatabase(String sql) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                return resultSet.getString(1);
            }

        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return null;
    }

    public String getNextLinkThenDelete() throws SQLException {
        String currentHandleLink = getNextLinkFromDatabase("select link from LINKS_TO_BE_PROCESSED limit 1");

        if (currentHandleLink == null) {
            System.out.println("待处理的链接池已经处理完毕，结束抓取");
            return null;
        }
        // 从数据库里删除该link
        updateLinkIntoDatabase(currentHandleLink);
        return currentHandleLink;
    }

    public void updateLinkIntoDatabase(String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("delete from LINKS_TO_BE_PROCESSED where link = ?")) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    public void insertProcessedLink(String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into LINKS_ALREADY_PROCESSED (link) values(?)")) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    public void insertToBeProcessedLink(String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into LINKS_TO_BE_PROCESSED (link) values(?)")) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    public void insertNewsIntoDatabase(String url, String content, String title) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into news (url,content,title,created_at,modified_at) values(?,?,?,now(),now())")) {
            statement.setString(1, url);
            statement.setString(2, content);
            statement.setString(3, title);
            statement.executeUpdate();
        }
    }

    public boolean isLinkProcessed(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }
}
