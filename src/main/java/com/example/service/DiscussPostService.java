package com.example.service;

import com.example.entity.DiscussPost;
import com.example.mapper.DiscussPostMapper;
import com.example.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    /**
     * 过滤敏感词
     */
    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 查询所有分页
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<DiscussPost> selectDiscussPost(int userId,int offset,int limit){
        return discussPostMapper.selectDiscussPost(userId, offset, limit);
    }

    /**
     * 查询总页数
     * @param userId
     * @return
     */
    public int selectDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 增加帖子
     * @param post
     * @return
     */
    public int insertDiscussPost(DiscussPost post){
        if (post == null){
            throw new IllegalArgumentException("参数不能为空!");
        }

//        转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
//        过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    /**
     * 帖子消息
     * @param id
     * @return
     */
    public DiscussPost selectDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    /**
     * 更新评论数
     * @param id
     * @param commentCount
     * @return
     */
    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }
}
