package com.hotpot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotpot.common.BusinessException;
import com.hotpot.common.PageResult;
import com.hotpot.dto.EmployeeDTO;
import com.hotpot.entity.SysEmployee;
import com.hotpot.mapper.SysEmployeeMapper;
import com.hotpot.service.EmployeeService;
import com.hotpot.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final SysEmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<EmployeeVO> list(Integer page, Integer size, String keyword, String role) {
        LambdaQueryWrapper<SysEmployee> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysEmployee::getRealName, keyword)
                    .or().like(SysEmployee::getUsername, keyword)
                    .or().like(SysEmployee::getPhone, keyword));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(SysEmployee::getRole, role);
        }
        wrapper.orderByDesc(SysEmployee::getCreatedAt);
        Page<SysEmployee> result = employeeMapper.selectPage(new Page<>(page, size), wrapper);
        List<EmployeeVO> voList = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(voList, result.getTotal(), page, size);
    }

    @Override
    public EmployeeVO getById(Long id) {
        SysEmployee employee = employeeMapper.selectById(id);
        if (employee == null) throw new BusinessException(404, "员工不存在");
        return toVO(employee);
    }

    @Override
    public void create(EmployeeDTO dto) {
        Long count = employeeMapper.selectCount(
                new LambdaQueryWrapper<SysEmployee>().eq(SysEmployee::getUsername, dto.getUsername()));
        if (count > 0) throw new BusinessException("用户名已存在");

        SysEmployee employee = new SysEmployee();
        BeanUtils.copyProperties(dto, employee);
        employee.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
        employee.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        employeeMapper.insert(employee);
    }

    @Override
    public void update(Long id, EmployeeDTO dto) {
        SysEmployee employee = employeeMapper.selectById(id);
        if (employee == null) throw new BusinessException(404, "员工不存在");

        if (!employee.getUsername().equals(dto.getUsername())) {
            Long count = employeeMapper.selectCount(
                    new LambdaQueryWrapper<SysEmployee>().eq(SysEmployee::getUsername, dto.getUsername()));
            if (count > 0) throw new BusinessException("用户名已存在");
        }

        BeanUtils.copyProperties(dto, employee, "password");
        if (StringUtils.hasText(dto.getPassword())) {
            employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        employeeMapper.updateById(employee);
    }

    @Override
    public void delete(Long id) {
        employeeMapper.deleteById(id);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        SysEmployee employee = employeeMapper.selectById(id);
        if (employee == null) throw new BusinessException(404, "员工不存在");
        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeMapper.updateById(employee);
    }

    private EmployeeVO toVO(SysEmployee entity) {
        EmployeeVO vo = new EmployeeVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
