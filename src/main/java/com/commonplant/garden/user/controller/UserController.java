package com.commonplant.garden.user.controller;

import com.commonplant.garden.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController

public class UserController {
    private final UserService userService;

}
