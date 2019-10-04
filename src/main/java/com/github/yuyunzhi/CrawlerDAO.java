package com.github.yuyunzhi;

import java.sql.SQLException;

public interface CrawlerDAO {

    String getNextLinkThenDelete() throws SQLException;

    void insertNewsIntoDatabase(String url, String content, String title) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

    void insertProcessedLink(String link) throws SQLException;

    void insertToBeProcessedLink(String link) throws SQLException;

}
