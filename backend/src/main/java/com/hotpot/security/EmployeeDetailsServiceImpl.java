package com.hotpot.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotpot.entity.SysEmployee;
import com.hotpot.mapper.SysEmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeDetailsServiceImpl implements UserDetailsService {

    private final SysEmployeeMapper employeeMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysEmployee employee = employeeMapper.selectOne(
                new LambdaQueryWrapper<SysEmployee>().eq(SysEmployee::getUsername, username));
        if (employee == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new LoginUser(employee);
    }
}
