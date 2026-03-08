package com.hotpot.controller.admin;

import com.hotpot.common.PageResult;
import com.hotpot.common.Result;
import com.hotpot.dto.ReservationDTO;
import com.hotpot.entity.TableReservation;
import com.hotpot.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public Result<PageResult<TableReservation>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(reservationService.list(page, size, status, keyword));
    }

    @GetMapping("/{id}")
    public Result<TableReservation> getById(@PathVariable Long id) {
        return Result.success(reservationService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody ReservationDTO dto) {
        reservationService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ReservationDTO dto) {
        reservationService.update(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return Result.success();
    }

    @PutMapping("/{id}/arrive")
    public Result<Void> arrive(@PathVariable Long id) {
        reservationService.arrive(id);
        return Result.success();
    }
}
