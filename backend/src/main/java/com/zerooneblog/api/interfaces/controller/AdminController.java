package com.zerooneblog.api.interfaces.controller;

import com.zerooneblog.api.service.AdminService;
import com.zerooneblog.api.service.PostService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
}