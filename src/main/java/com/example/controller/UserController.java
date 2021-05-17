package com.example.controller;

import com.example.annotation.LginRequired;
import com.example.entity.User;
import com.example.service.FollowService;
import com.example.service.LikeService;
import com.example.service.UserService;
import com.example.utils.CommunityConstantUtil;
import com.example.utils.CommunityUtil;
import com.example.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstantUtil {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    /**
     *访问账号设置页面
     * @return
     */
    @LginRequired
    @RequestMapping(value = "/setting",method = RequestMethod.GET)
    public String toSetting(){
        return "site/setting";
    }

    /**
     * 头像上传
     * @return
     */
    @LginRequired
    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage == null){
            model.addAttribute("error","您还没有选择图片!");
            return "site/setting";
        }
//        用户传入的文件名
        String filename = headerImage.getOriginalFilename();
//        用户传入图片的后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
//      判断图片是否有后缀
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确!");
            return "site/setting";
        }

//        生成随机的图片名字
        filename = CommunityUtil.generateUUID() + suffix;
//        确定文件存放的路径
        File dest = new File(uploadPath + "/" + filename);
        try {
//            存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发送异常!",e);
        }

//        更新当前用户的头像的路径(web)
//        http://localhost:8080/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(value = "/header/{filename}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response){
//      服务器存放的路径
        filename = uploadPath + "/" + filename;
//        文件的后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
//        响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(filename);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
           logger.error("读取头像失败" + e.getMessage());
        }
    }


    /**
     * 修改密码功能
     * @param password
     * @param newPassword
     * @param model
     * @return
     */
    @RequestMapping(value = "/updatePassword",method = RequestMethod.POST)
    public String updatePassword(String password,String newPassword,Model model){
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(password, newPassword, user.getId());
        if (map == null || map.isEmpty()){
            return "redirect:/index";
        }else {
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
            return "site/setting";
        }
    }


    /**
     * 个人信息
     * @param userId
     * @param model
     * @return
     */
    @RequestMapping(value = "/profile/{userId}",method = RequestMethod.GET)
    public String getProFilePage(@PathVariable("userId") int userId,Model model){
        User user = hostHolder.getUser();
        if (user == null){
            throw new RuntimeException("该用户不存在!");
        }
//        用户
        if (user.getId() == userId) {
            model.addAttribute("user", user);
        }else {
            model.addAttribute("user",userService.selectById(userId));
        }
//        点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
//        查询粉丝的数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
//        是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "site/profile";
    }


    public void send(String path){

    }
}
