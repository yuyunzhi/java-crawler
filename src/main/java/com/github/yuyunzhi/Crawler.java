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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Crawler extends Thread {

    private CrawlerDAO dao;

    public Crawler(CrawlerDAO dao) {
        this.dao = new MybatisCrawlerDao();
    }

    @Override
    public void run(){
        try{
            String currentHandleLink;
            // 从数据库里取出待处理的一条link 能加载到就进入循环，且取出后在数据库里删除这条link
            // 第一次进去会默认有一个初始化url的，配置在 db/migration/V2__Init_data.sql
            while ((currentHandleLink = dao.getNextLinkThenDelete()) != null) {

                // 询问数据库LINKS_ALREADY_PROCESSED，当前链接是否被处理过了？即是否在里面
                if (dao.isLinkProcessed(currentHandleLink)) {
                    continue;
                }

                if (isInterestingLink(currentHandleLink)) {
                    Document doc = getHttpAndParseHtml(currentHandleLink);

                    // 解析获取的页面的link，并把解析后的link存到数据库 LINKS_TO_BE_PROCESSED
                    parseUrlFromPageAndStoreIntoDatabase(doc);

                    // 如果这是一个新闻页面，就提取新闻内容页面的数据存入数据库中 NEWS
                    saveDataBaseIfItIsNewsPage(doc, currentHandleLink);

                    // 处理完连接后，把处理的这条链接放入数据库中 LINKS_ALREADY_PROCESSED
                    dao.insertProcessedLink(currentHandleLink);

                }
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }

    }


    private void parseUrlFromPageAndStoreIntoDatabase(Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            if (!href.toLowerCase().startsWith("javascript")) {
                dao.insertToBeProcessedLink(href);
               // dao.updateLinkIntoDatabase(href, "insert into LINKS_TO_BE_PROCESSED (link) values(?)");
            }
        }
    }

    private void saveDataBaseIfItIsNewsPage(Document doc, String link) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                ArrayList<Element> paragraphs = articleTag.select("p");
                String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));

                dao.insertNewsIntoDatabase(link, content, title);
                System.out.println("link = " + link);
            }

        }

    }

    private static Document getHttpAndParseHtml(String link) throws IOException {

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");

        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
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
