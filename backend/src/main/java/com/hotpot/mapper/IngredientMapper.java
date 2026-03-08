package com.hotpot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotpot.entity.Ingredient;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IngredientMapper extends BaseMapper<Ingredient> {
}
