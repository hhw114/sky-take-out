package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.core.annotation.Order;

import java.util.List;

@Mapper
public interface OrderMapper {
    /*
    * 插入订单数据
    * */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /*
    * 查询当前用户所有订单信息
    *
    * */
    List<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /*
    * 根据id查询order表里的信息
    * */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /*
    * 管理端搜索订单
    * */
    List<Orders> adminpageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("SELECT " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS toBeConfirmed, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS confirmed, " +
            "SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) AS deliveryInProgress " +
            "FROM orders")
    OrderStatisticsVO statics();
}
