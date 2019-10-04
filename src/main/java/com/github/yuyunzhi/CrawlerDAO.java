package com.github.yuyunzhi;

import java.sql.SQLException;

public interface CrawlerDAO {

    String getNextLinkFromDatabase(String sql) throws SQLException;

    String getNextLinkThenDelete() throws SQLException;

    void updateLinkIntoDatabase(String link, String sql) throws SQLException;

    void insertNewsIntoDatabase(String url,String content,String title) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

}
