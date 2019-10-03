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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        // 待处理的链接池
        List<String> linkPool = new ArrayList<>();
        // 已经处理过的链接池
        Set<String> processedLinks = new HashSet<>();

        linkPool.add("https://sina.cn");

        while(true){

            if(linkPool.isEmpty()){
                break;
            }

            String link = linkPool.remove(linkPool.size()-1);

            if(processedLinks.contains(link)){
                continue;
            }


            if((link.contains("news.sina.cn") && !link.contains("passport.sina.cn") )|| "https://sina.cn".equals(link)){

                if(link.startsWith("//")){ link = "https:"+link; }
                System.out.println("link = " + link);
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(link);
                httpGet.addHeader("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");

                try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
                    System.out.println(response1.getStatusLine());
                    HttpEntity entity1 = response1.getEntity();
                    String html = EntityUtils.toString(entity1);

                    Document doc = Jsoup.parse(html);

                    ArrayList<Element> links = doc.select("a");

                    for (Element aTag: links) {
                        linkPool.add(aTag.attr("href"));
                    }
                    ArrayList<Element> articleTags = doc.select("article");

                    if(!articleTags.isEmpty()){

                        for (Element articleTag:articleTags) {
                            String title  = articleTags.get(0).child(0).text();
                            System.out.println("title = " + title);
                        }

                    }

                    // 处理完连接后，把处理后的链接放入processedLinks
                    processedLinks.add(link);
                }
            }
        }



    }
}
