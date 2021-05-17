package com.example.service;

import com.example.entity.LoginTicket;
import com.example.entity.User;
import com.example.mapper.LoginTicketMapper;
import com.example.mapper.UserMapper;
import com.example.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstantUtil{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 邮箱发送
     */
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User selectById(int id){
//      return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null){
           user = initCache(id);
        }
        return user;
    }

    /**
     * 注册业务
     * @param user
     * @return
     */
    public Map<String,Object> register(User user){
        Map<String, Object> map = new HashMap<>();

//        空值处理
        if (user == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空!");
            return map;
        }
//        验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null){
            map.put("usernameMsg","该账号已存在!");
            return map;
        }

//        验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null){
            map.put("emailMsg","该邮箱已存在!");
            return map;
        }

//        注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

//      激活邮箱
        Context context = new Context();
        User users = userMapper.selectByEmail(user.getEmail());
        context.setVariable("email",users.getEmail());
        //http://localhost:8080/activation/101/code
        String url = domain + contextPath + "/activation/" + users.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(users.getEmail(),"激活账号",content);

        return map;
    }

    /**
     * 账号激活业务
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else {
           return ACTIVATION_FAILURE;
        }
    }


    /**
     * 登录业务
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

//        判断空值
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空!");
            return map;
        }

//        验证账号
        User user = userMapper.selectByName(username);
        if (user == null){
            map.put("usernameMsg","该账号不存在!");
            return map;
        }
        if (user.getStatus() == 0){
            map.put("usernameMsg","该账户未激活!");
            return map;
        }
//        验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确!");
            return map;
        }

//        生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey = RedisKeyUtils.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());

        return map;
    }


    /**
     * 退出业务
     * @param ticket
     */
    public void logout(String ticket){
//        loginTicketMapper.updateStats(ticket,1);
        String redisKey = RedisKeyUtils.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    /**
     * 查询凭证
     * @param ticket
     * @return
     */
    public LoginTicket loginTicket(String ticket){
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtils.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    /**
     * 根据用户id查询用户信息
     * @param id
     * @return
     */
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    /**
     * 上传头像
     * @param userId
     * @param headerUrl
     * @return
     */

    public int updateHeader(int userId,String headerUrl){
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }


    /**
     * 修改密码
     * @param password
     * @return
     */
    public Map<String,Object> updatePassword(String password,String newPassword,int id){
        Map<String, Object> map = new HashMap<>();

//        判断空值
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","请输入原始密码!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg","请输入新的密码!");
            return map;
        }

        User user = userMapper.selectById(id);
        password = CommunityUtil.md5(password + user.getSalt());

        if (!user.getPassword().equals(password)){
            map.put("passwordMsg","请输入正确的原始密码!");
            return map;
        }
        if (password.equals(newPassword)){
            map.put("passwordMsg","不能使用最近使用过的密码!");
            return map;
        }

//        验证通过
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(id,newPassword);

        return map;
    }


    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

//    优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtils.getUserKey(userId);
       return (User) redisTemplate.opsForValue().get(redisKey);
    }
//    取不到时，初始化缓存数据
    public User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtils.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

//    数据变更时清除缓存数据
    public void clearCache(int userId){
        String redisKey = RedisKeyUtils.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

}
