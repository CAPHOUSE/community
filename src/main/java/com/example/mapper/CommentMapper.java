package com.example.mapper;

import com.example.entity.Comment;

import java.util.List;

public interface CommentMapper {

    List<Comment> selectComment(int entityType,int entityId,int offset,int limit);

    int selectCommentEntity(int entityType,int entityId);

    int insertComment(Comment comment);
}
