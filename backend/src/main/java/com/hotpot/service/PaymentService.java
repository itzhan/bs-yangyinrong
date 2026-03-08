package com.hotpot.service;

import com.hotpot.common.PageResult;
import com.hotpot.dto.PaymentDTO;
import com.hotpot.entity.Payment;

public interface PaymentService {
    PageResult<Payment> list(Integer page, Integer size, Integer paymentMethod, String keyword);
    Payment getById(Long id);
    Payment pay(PaymentDTO dto);
    void refund(Long id);
}
