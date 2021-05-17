package com.example.service;

import com.example.entity.Message;
import com.example.mapper.MessageMapper;
import com.example.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> selectConversations(int userId,int offset,int limit){
        return messageMapper.selectConversations(userId,offset,limit);
    }

    public int selectConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> selectLetters(String conversationId,int offset,int limit){
        return messageMapper.selectLetters(conversationId,offset,limit);
    }

    public int selectLetterCount(String conversationId){
        return messageMapper.selectLetterCount(conversationId);
    }

    public int selectLetterUnreadCount(int userId,String conversationId){
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    public int insertMessage(Message message){
        message.setContent(sensitiveFilter.filter(message.getContent()));
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));

        return messageMapper.insertMessage(message);
    }

    public int readMessage(List<Integer> ids){
        return messageMapper.updateStatus(ids,1);
    }
}
