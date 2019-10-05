package com.github.yuyunzhi;

public class Main {
    public static void main(String[] args) {
        CrawlerDAO dao =new MybatisCrawlerDao();

        for(int i = 0;i<3;i++){
            new Crawler(dao).start();
        }
    }
}
