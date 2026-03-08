package com.hotpot.controller.admin;

import com.hotpot.common.PageResult;
import com.hotpot.common.Result;
import com.hotpot.dto.PaymentDTO;
import com.hotpot.entity.Payment;
import com.hotpot.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public Result<PageResult<Payment>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer paymentMethod,
            @RequestParam(required = false) String keyword) {
        return Result.success(paymentService.list(page, size, paymentMethod, keyword));
    }

    @GetMapping("/{id}")
    public Result<Payment> getById(@PathVariable Long id) {
        return Result.success(paymentService.getById(id));
    }

    @PostMapping
    public Result<Payment> pay(@Valid @RequestBody PaymentDTO dto) {
        return Result.success(paymentService.pay(dto));
    }

    @PutMapping("/{id}/refund")
    public Result<Void> refund(@PathVariable Long id) {
        paymentService.refund(id);
        return Result.success();
    }
}
