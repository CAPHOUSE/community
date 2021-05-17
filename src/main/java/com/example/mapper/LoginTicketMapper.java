package com.example.mapper;

import com.example.entity.LoginTicket;

@Deprecated  //不推荐使用
public interface LoginTicketMapper {

    int insertLoginTicket(LoginTicket loginTicket);

    LoginTicket selectByTicket(String ticket);

    int updateStats(String ticket,int status);


}
