package controller;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class Servlet {

    @GetMapping(value = "/game.html")
    public String handle(Model model, HttpSession session) {
        model.addAttribute("sid", session.getId());
        return "ballBeam";
    }

}
