package com.comp512.ballBeam.controller;

import com.comp512.ballBeam.bean.User;
import com.comp512.ballBeam.services.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserServices userServices;

    @Autowired
    public UserController(UserServices userServices) {
        this.userServices = userServices;
    }

    @RequestMapping(value = "/defaultRegistration")
    public String aaa(){
        for (int i = 0; i < 10000; i++) {
            User user = new User();
            user.setUsername(String.valueOf(i));
            user.setPassword("9999");
            user.setRole("USER");
            user.setHighestPoint(0);
            userServices.registerUser(user);
        }
        return "forward:/rooms";
    }

    @PostMapping(value = "/registration")
    public ModelAndView signUpUser(@RequestParam String username, @RequestParam String password) {
        ModelAndView mv = new ModelAndView();
        User user = new User(username, password);
        if (userServices.exist(user.getUsername())) {
            mv.addObject("error", true);
            mv.addObject("reason", "username already existed");
            mv.setViewName("signup");
        } else {
            userServices.registerUser(user);
            mv.setViewName("home");
        }
        logger.info(mv.getViewName());
        return mv;
    }
}
