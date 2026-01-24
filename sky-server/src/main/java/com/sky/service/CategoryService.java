package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.result.PageResult;

public interface CategoryService {
    void addCategory(CategoryDTO categoryDTO);

    void startOrStop(Integer status, Integer id);

    PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);

    void update(CategoryDTO categoryDTO);

    void deleteById(Integer id);

    PageResult list(Integer type);
}
