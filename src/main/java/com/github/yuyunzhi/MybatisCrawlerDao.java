package com.github.yuyunzhi;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MybatisCrawlerDao implements CrawlerDAO {
    private SqlSessionFactory sqlSessionFactory;


    public MybatisCrawlerDao() {
        try{
            String resource = "mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        }catch(IOException e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getNextLinkThenDelete() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String link = session.selectOne("com.github.yuyunzhi.MyMapper.selectNextAvailableLink");
            if(link !=null){
                session.delete("com.github.yuyunzhi.MyMapper.deleteLink",link);

            }
            return link;
        }
    }

    @Override
    public void insertProcessedLink(String link)  {
        //"insert into LINKS_ALREADY_PROCESSED (link) values(#link)"

        Map<String,Object> param = new HashMap<>();
        param.put("tableName","LINKS_ALREADY_PROCESSED");
        param.put("link",link);
        try(SqlSession session = sqlSessionFactory.openSession(true)){
            session.insert("com.github.yuyunzhi.MyMapper.insertLink",param);

        }
    }

    @Override
    public void insertToBeProcessedLink(String link)  {
        // "insert into LINKS_TO_BE_PROCESSED (link) values(#link)"
        Map<String,Object> param = new HashMap<>();
        param.put("tableName","LINKS_TO_BE_PROCESSED");
        param.put("link",link);
        try(SqlSession session = sqlSessionFactory.openSession(true)){
            session.insert("com.github.yuyunzhi.MyMapper.insertLink",param);

        }
    }

    @Override
    public void insertNewsIntoDatabase(String url, String content, String title) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.yuyunzhi.MyMapper.insertNews",new News(url,title,content));
        }
    }

    @Override
    public boolean isLinkProcessed(String link)  {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
           int count = (Integer) session.selectOne("com.github.yuyunzhi.MyMapper.countLink",link);
           return count !=0;
        }
    }
}
