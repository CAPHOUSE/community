package com.example.mapper;

import com.example.entity.Message;

import java.util.List;

public interface MessageMapper {

//    查询当前用户会话，返回一条最新的私信
    List<Message> selectConversations(int userId,int offset,int limit);

//  查询当前用户的会话数量
    int selectConversationCount(int userId);

//    查询某个会话的私信列表
    List<Message> selectLetters(String conversationId,int offset,int limit);

//    查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

//    查询未读私信的数量
    int selectLetterUnreadCount(int userId,String conversationId);

//    新增消息
    int insertMessage(Message message);

//  已读功能
    int updateStatus(List<Integer> ids,int status);
}
