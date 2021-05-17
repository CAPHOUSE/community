package com.example;

import com.example.entity.DiscussPost;
import com.example.entity.Message;
import com.example.mapper.DiscussPostMapper;
import com.example.mapper.MessageMapper;
import com.example.utils.CommunityUtil;
import com.example.utils.MailClient;
import com.example.utils.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPost(0, 0, 10);
        for (DiscussPost post : list){
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    @Test
    void test2(){
        mailClient.sendMail("2305211762@qq.com","test","welcome to java");
    }

    @Test
    void test03(){
        String s = CommunityUtil.md5("12345" + "e2eb3");
        System.out.println(s);
    }


    @Test
    void test5(){
        String text = "这里可以赌*博";
        String filter = sensitiveFilter.filter(text);
        System.out.println(filter);
    }

    @Test
    void test6(){
        List<Message> messages = messageMapper.selectConversations(111, 0, 10);
        for (Message message : messages) {
            System.out.println(message);
        }
        int count = messageMapper.selectConversationCount(112);
        System.out.println(count);

        int count1 = messageMapper.selectLetterCount("111_112");
        System.out.println(count1);

        List<Message> messages1 = messageMapper.selectLetters("111_112", 0, 20);
        for (Message message : messages1) {
            System.out.println(message);
        }

        int count2 = messageMapper.selectLetterUnreadCount(111, null);
        System.out.println(count2);

    }


    @Test
    void test8(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPost(0, 1, 10);
        System.out.println(discussPosts);
    }

}
