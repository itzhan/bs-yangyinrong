package com.hotpot.service;

import com.hotpot.dto.DiningTableDTO;
import com.hotpot.entity.DiningTable;
import java.util.List;

public interface DiningTableService {
    List<DiningTable> list(String area, Integer status);
    DiningTable getById(Long id);
    void create(DiningTableDTO dto);
    void update(Long id, DiningTableDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
}
