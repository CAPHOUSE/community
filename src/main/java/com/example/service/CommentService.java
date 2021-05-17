package com.example.service;

import com.example.entity.Comment;
import com.example.mapper.CommentMapper;
import com.example.mapper.DiscussPostMapper;
import com.example.utils.CommunityConstantUtil;
import com.example.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstantUtil {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 显示评论
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    public List<Comment> selectComment(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectComment(entityType, entityId, offset, limit);
    }

    public int selectCommentEntity(int entityType, int entityId) {
        return commentMapper.selectCommentEntity(entityType, entityId);
    }

    /**
     * 添加父级评论
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)  //添加事务，要么一起成功，要么一起失败
    public int addCommentCount(Comment comment){
        if (comment == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
//        转义HTML标签
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
//       过滤敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

//
        int row = commentMapper.insertComment(comment);
//      跟新帖子的评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST){
//            查询帖子为entitytype为1的总数量
            int count = commentMapper.selectCommentEntity(comment.getEntityType(), comment.getEntityId());
            discussPostMapper.updateCommentCount(comment.getId(),count);
        }
        return row;
    }
}
