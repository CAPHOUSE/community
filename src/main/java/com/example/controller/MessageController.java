package com.example.controller;

import com.example.entity.Message;
import com.example.entity.Page;
import com.example.entity.User;
import com.example.service.MessageService;
import com.example.service.UserService;
import com.example.utils.CommunityUtil;
import com.example.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 消息列表
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(value = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.selectConversationCount(user.getId()));

        List<Message> conversationList = messageService.selectConversations(user.getId(), page.getOffset(), page.getLimit());

        List<Map<String,Object>> conversations = new ArrayList<>();
        if (conversationList != null){
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.selectLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.selectLetterUnreadCount(user.getId(),message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target",userService.selectById(targetId));

                conversations.add(map);
            }
        }

        model.addAttribute("conversations",conversations);

//        查询未读消息数量
        int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        return "site/letter";
    }

    /**
     *
     * @param conversationId
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(value = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Model model,Page page){
//        分页信息设置
        page.setLimit(5);
        page.setPath("/letter/detail/"+ conversationId);
        page.setRows(messageService.selectLetterCount(conversationId));

//        私信列表
        List<Message> letterList = messageService.selectLetters(conversationId, page.getOffset(), page.getLimit());

        ArrayList<Map<String,Object>> letters = new ArrayList<>();
        if (letterList != null){
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.selectById(message.getFromId()));

                letters.add(map);
            }
        }

        model.addAttribute("letters",letters);

//      查询私信的目标
        model.addAttribute("target",getLetterTarget(conversationId));

//      设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "site/letter-detail";
    }


    /**
     * 判断目标id
     * @param conversationId
     * @return
     */
    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0){
            return userService.selectById(id1);
        }else {
            return userService.selectById(id0);

        }
    }

    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer>ids = new ArrayList<>();

        if(letterList != null){
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    /**
     * 发送私信
     * @param toName
     * @param content
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/letter/send",method = RequestMethod.POST)
    public String sendLetter(String toName,String content){
        User target = userService.findUserByName(toName);
        if(target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在!");
        }

        Message message = new Message();
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        messageService.insertMessage(message);
        return CommunityUtil.getJSONString(0);
    }
}
