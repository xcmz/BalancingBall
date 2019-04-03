package com.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
public class Servlet {

    @GetMapping(value = "/index.html")
    public String handle(Model model, HttpSession session) {
        model.addAttribute("sid", session.getId());
        return "ballBeam";
    }

}
