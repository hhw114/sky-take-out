package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private CategoryMapper categoryMapper;
    @Transactional
    @Override
    public void save(SetmealDTO setmealDTO) {
        //封装一个setmeal，注入setmeal表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.save(setmeal);
        //回显setmealid
        Long setmealId = setmeal.getId();


        //封装DTO中的数个setmealdish，注入setmeal_dish表

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.save(setmealDishes);
    }

    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        Page p = (Page) setmealMapper.pageQuery(setmealPageQueryDTO);

        long total=p.getTotal();

        List setmealList=p.getResult();

        PageResult pageResult =new PageResult(total,setmealList);

        return pageResult;
    }

    @Override
    public void startOrStop(Long status, Long id) {
        setmealMapper.startOrStop(status,id);
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        //判断是否起售，如果起售不能删除
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if(StatusConstant.ENABLE == setmeal.getStatus()){
                //起售中的套餐不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        //删除setmeal表里的内容
        setmealMapper.deleteBatch(ids);
        //删除setmeal_dish表里的内容
        setmealDishMapper.deleteBatch(ids);
    }

    @Override
    public SetmealVO getById(Long id) {
        //先查询setmeal表
        Setmeal setmeal=new Setmeal();
        setmeal=setmealMapper.getById(id);
        //再查询setmeal_dish表
        List<SetmealDish> list =setmealDishMapper.getById(id);
        //查询category表
        String categoryName=categoryMapper.getNameById(setmeal.getCategoryId());
        //将三次查询数据封入setmealvo
        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(list);
        setmealVO.setCategoryName(categoryName);
        return setmealVO;
    }

    @Override
    public void update(SetmealDTO setmealDTO) {
        //更新setmeal表中数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);
        //删除setmeal_dish表中数据
        setmealDishMapper.deleteBatch(Collections.singletonList(setmealDTO.getId()));
        //插入setmeal_dish表中数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDTO.getId());
        });
        setmealDishMapper.save(setmealDishes);
    }
}
