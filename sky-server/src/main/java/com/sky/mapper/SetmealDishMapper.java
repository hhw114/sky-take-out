package com.sky.mapper;


import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);


    void save(List<SetmealDish> setmealDishes);

    void deleteBatch(List<Long> ids);

    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getById(Long setmealId);
}
