package com.dipa.notefournote.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboardPage() {
        return "dashboard";
    }

}