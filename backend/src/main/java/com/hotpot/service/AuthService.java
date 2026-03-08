package com.hotpot.service;

import com.hotpot.dto.LoginDTO;
import com.hotpot.vo.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO dto);
}
