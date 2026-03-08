package com.hotpot.service.impl;

import com.hotpot.common.BusinessException;
import com.hotpot.dto.LoginDTO;
import com.hotpot.entity.SysEmployee;
import com.hotpot.security.JwtUtils;
import com.hotpot.security.LoginUser;
import com.hotpot.service.AuthService;
import com.hotpot.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public LoginVO login(LoginDTO dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            SysEmployee employee = loginUser.getEmployee();
            String token = jwtUtils.generateToken(employee.getId(), employee.getUsername(), employee.getRole());

            LoginVO vo = new LoginVO();
            vo.setToken(token);
            vo.setUserId(employee.getId());
            vo.setUsername(employee.getUsername());
            vo.setRealName(employee.getRealName());
            vo.setRole(employee.getRole());
            vo.setAvatar(employee.getAvatar());
            return vo;
        } catch (BadCredentialsException e) {
            throw new BusinessException(401, "用户名或密码错误");
        }
    }
}
