package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/admin/order")
@Api(tags = "订单管理相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @ApiOperation("订单搜索")
    @GetMapping("/conditionSearch")
    public Result<PageResult> orderSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("管理端订单搜索，搜索条件为:{}",ordersPageQueryDTO);
        PageResult pageResult=orderService.adminPageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
}
