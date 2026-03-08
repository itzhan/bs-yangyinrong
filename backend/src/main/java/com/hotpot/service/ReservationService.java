package com.hotpot.service;

import com.hotpot.common.PageResult;
import com.hotpot.dto.ReservationDTO;
import com.hotpot.entity.TableReservation;

public interface ReservationService {
    PageResult<TableReservation> list(Integer page, Integer size, Integer status, String keyword);
    TableReservation getById(Long id);
    void create(ReservationDTO dto);
    void update(Long id, ReservationDTO dto);
    void cancel(Long id);
    void arrive(Long id);
}
