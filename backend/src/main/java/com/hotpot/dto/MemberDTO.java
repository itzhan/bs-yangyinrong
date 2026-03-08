package com.hotpot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MemberDTO {
    @NotBlank(message = "姓名不能为空")
    private String name;
    @NotBlank(message = "手机号不能为空")
    private String phone;
    private Integer gender;
    private LocalDate birthday;
    private Integer status;
}
