package com.hotpot.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmployeeVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String role;
    private String avatar;
    private Integer status;
    private LocalDateTime createdAt;
}
