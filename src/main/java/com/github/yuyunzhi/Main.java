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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String USER_NAME = "root";
    private static final String PASS_WORD = "root";

    public static void main(String[] args) throws IOException, SQLException {

        Connection connection = DriverManager.getConnection("jdbc:h2:file:/Users/yuyunzhi/Desktop/java-crawler/crawler", USER_NAME, PASS_WORD);

        while (true) {

            // 待处理的链接池  第一次进去会默认有一个初始化url的，配置在 db/migration/V2__Init_data.sql
            List<String> linkPool = loadUrlsFromDatabase(connection, "select link from LINKS_TO_BE_PROCESSED");

            if (linkPool.isEmpty()) {
                System.out.println("待处理的链接池已经处理完毕，结束抓取");
                break;
            }

            String link = linkPool.remove(linkPool.size() - 1);
            try (PreparedStatement statement = connection.prepareStatement("delete from LINKS_TO_BE_PROCESSED where link = ?")) {
                statement.setString(1, link);
                statement.executeUpdate();
            }


            // 询问数据库LINKS_ALREADY_PROCESSED，当前链接是否被处理过了？即是否在里面
            if (isLinkProcessed(connection, link)) {
                continue;
            }


            if (isInterestingLink(link)) {
                Document doc = getHttpAndParseHtml(link);

                // 解析获取的页面的link，并把解析后的link存到数据库 LINKS_TO_BE_PROCESSED
                parseUrlFromPageAndStoreIntoDatabase(connection, doc);

                // 如果这是一个新闻页面，就提取新闻内容页面的数据存入数据库中 NEWS
                saveDataBaseIfItIsNewsPage(doc);

                // 处理完连接后，把处理的这条链接放入数据库中 LINKS_ALREADY_PROCESSED
                insertLinkIntoDatabase(connection, link, "insert into LINKS_ALREADY_PROCESSED (link) values(?)");

            }
        }


    }

    private static void parseUrlFromPageAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            System.out.println("href = " + href);
            insertLinkIntoDatabase(connection, href, "insert into LINKS_TO_BE_PROCESSED (link) values(?)");
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
            if(resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    private static void insertLinkIntoDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static List<String> loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {
        List<String> results = new ArrayList<>();
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                results.add(resultSet.getString(1));
            }

        } finally {
            if(resultSet != null) {
                resultSet.close();
            }
        }
        return results;
    }

    private static void saveDataBaseIfItIsNewsPage(Document doc) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                System.out.println("title = " + title);
            }

        }
    }

    private static Document getHttpAndParseHtml(String link) throws IOException {
        if (link.startsWith("//")) {
            link = "https:" + link;
        }

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
