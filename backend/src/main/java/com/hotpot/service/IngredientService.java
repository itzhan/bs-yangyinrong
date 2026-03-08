package com.hotpot.service;

import com.hotpot.common.PageResult;
import com.hotpot.dto.IngredientDTO;
import com.hotpot.dto.InventoryRecordDTO;
import com.hotpot.entity.Ingredient;
import com.hotpot.entity.InventoryRecord;
import java.util.List;

public interface IngredientService {
    PageResult<Ingredient> list(Integer page, Integer size, String keyword, String category);
    Ingredient getById(Long id);
    void create(IngredientDTO dto);
    void update(Long id, IngredientDTO dto);
    void delete(Long id);
    /** 低库存预警列表 */
    List<Ingredient> lowStockWarning();
    /** 出入库/盘点操作 */
    void operate(InventoryRecordDTO dto);
    /** 出入库记录 */
    PageResult<InventoryRecord> recordList(Integer page, Integer size, Long ingredientId, Integer type);
}
