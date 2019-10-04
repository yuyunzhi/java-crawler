package com.github.yuyunzhi;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final String USER_NAME = "root";
    private static final String PASS_WORD = "root";

    public static void main(String[] args) throws IOException, SQLException {

        Connection connection = DriverManager.getConnection("jdbc:h2:file:/Users/yuyunzhi/Desktop/java-crawler/crawler", USER_NAME, PASS_WORD);

        String currentHandleLink;
        // 从数据库里取出待处理的一条link 能加载到就进入循环，且取出后在数据库里删除这条link
        // 第一次进去会默认有一个初始化url的，配置在 db/migration/V2__Init_data.sql
        while ((currentHandleLink = getNextLinkThenDelete(connection)) != null) {

            // 询问数据库LINKS_ALREADY_PROCESSED，当前链接是否被处理过了？即是否在里面
            if (isLinkProcessed(connection, currentHandleLink)) {
                continue;
            }

            if (isInterestingLink(currentHandleLink)) {
                Document doc = getHttpAndParseHtml(currentHandleLink);

                // 解析获取的页面的link，并把解析后的link存到数据库 LINKS_TO_BE_PROCESSED
                parseUrlFromPageAndStoreIntoDatabase(connection, doc);

                // 如果这是一个新闻页面，就提取新闻内容页面的数据存入数据库中 NEWS
                saveDataBaseIfItIsNewsPage(connection, doc, currentHandleLink);

                // 处理完连接后，把处理的这条链接放入数据库中 LINKS_ALREADY_PROCESSED
                updateLinkIntoDatabase(connection, currentHandleLink, "insert into LINKS_ALREADY_PROCESSED (link) values(?)");

            }
        }


    }

    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        String currentHandleLink = getNextLinkFromDatabase(connection, "select link from LINKS_TO_BE_PROCESSED limit 1");

        if (currentHandleLink == null) {
            System.out.println("待处理的链接池已经处理完毕，结束抓取");
            return null;
        }
        // 从数据库里删除该link
        updateLinkIntoDatabase(connection, currentHandleLink, "delete from LINKS_TO_BE_PROCESSED where link = ?");
        return currentHandleLink;
    }

    private static void parseUrlFromPageAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            //System.out.println("href = " + href);
            if (href.startsWith("//")) {
                href = "https:" + href;
            }

            if (!href.toLowerCase().startsWith("javascript")) {
                updateLinkIntoDatabase(connection, href, "insert into LINKS_TO_BE_PROCESSED (link) values(?)");
            }
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
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

    private static void updateLinkIntoDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static String getNextLinkFromDatabase(Connection connection, String sql) throws SQLException {
        List<String> results = null;
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

    private static void saveDataBaseIfItIsNewsPage(Connection connection, Document doc, String link) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                ArrayList<Element> paragraphs = articleTag.select("p");
                String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));

                try (PreparedStatement statement = connection.prepareStatement("insert into news (url,content,title,created_at,modified_at) values(?,?,?,now(),now())")) {
                     statement.setString(1, link);
                     statement.setString(2, content);
                     statement.setString(3, title);
                     statement.executeUpdate();
                }
            System.out.println("link = " + link);
            System.out.println("title = " + title);
        }

    }

}

    private static Document getHttpAndParseHtml(String link) throws IOException {

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");

        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);

            return Jsoup.parse(html);
        }
    }

    private static boolean isInterestingLink(String link) {
        return (isNewsPage(link) && isNotLoginPage(link)) || isIndexPage(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }

    private static boolean isIndexPage(String link) {
        return "https://sina.cn".equals(link);
    }

}
