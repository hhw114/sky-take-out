package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/*
*
* 定时任务类
* */
@Slf4j
@Component
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /*
    * 处理超时订单
    * */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder(){
        log.info("定期处理超时订单:{}", LocalDateTime.now());
        //获得超时订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList=orderMapper.getByStatusAndTime(Orders.PENDING_PAYMENT,time);
        //取消这些超时订单，设置取消状态，取消原因
        if (ordersList != null && ordersList.size()>0){
            for(Orders order:ordersList){
                order.setStatus(Orders.CANCELLED);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("订单超时，自动取消");
                orderMapper.update(order);
            }
        }

    }

    /*
    * 每天处理未完成订单
    * *
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("定时处理派送中的订单:{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);//处理前一天所有在派送的订单
        List<Orders> ordersList=orderMapper.getByStatusAndTime(Orders.DELIVERY_IN_PROGRESS,time);
        //取消这些超时订单，设置为完成
        if (ordersList != null && ordersList.size()>0){
            for(Orders order:ordersList){
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}
