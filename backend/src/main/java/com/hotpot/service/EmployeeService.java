package com.hotpot.service;

import com.hotpot.common.PageResult;
import com.hotpot.dto.EmployeeDTO;
import com.hotpot.vo.EmployeeVO;

public interface EmployeeService {
    PageResult<EmployeeVO> list(Integer page, Integer size, String keyword, String role);
    EmployeeVO getById(Long id);
    void create(EmployeeDTO dto);
    void update(Long id, EmployeeDTO dto);
    void delete(Long id);
    void resetPassword(Long id, String newPassword);
}
