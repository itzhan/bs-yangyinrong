package com.hotpot.controller;

import com.hotpot.common.Result;
import com.hotpot.common.SecurityUtils;
import com.hotpot.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error(401, "未登录");
        }
        Map<String, Object> info = new HashMap<>();
        info.put("userId", loginUser.getEmployee().getId());
        info.put("userName", loginUser.getEmployee().getRealName());
        info.put("roles", List.of("ROLE_" + loginUser.getEmployee().getRole()));
        info.put("buttons", List.of());
        info.put("avatar", loginUser.getEmployee().getAvatar());
        info.put("email", "");
        return Result.success(info);
    }
}
