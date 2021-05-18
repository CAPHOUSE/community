package com.example.controller;

import com.example.entity.Comment;
import com.example.service.CommentService;
import com.example.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(value = "/add/{id}",method = RequestMethod.POST)
    public String addComment(@PathVariable("id") int id, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);

//       添加
        commentService.addCommentCount(comment);

        return "redirect:/discuss/detail/" + id;
    }
}
