package com.hotpot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeeDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String password;
    @NotBlank(message = "姓名不能为空")
    private String realName;
    private String phone;
    @NotBlank(message = "角色不能为空")
    private String role;
    private String avatar;
    private Integer status;
}
