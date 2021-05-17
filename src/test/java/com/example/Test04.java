package com.example;

import com.example.entity.DiscussPost;
import com.example.mapper.DiscussPostMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class Test04 {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void test(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPost(0, 1, 10);
        System.out.println(discussPosts);
    }
}
