package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);


    /*
    * 查询历史订单
    * */
    PageResult listHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /*
    * 根据id查询订单明细
    * */
    OrderVO getOrderDetail(Long id);

    /*
    * 用户端取消订单
    * */
    void userCancel(Long id) throws Exception;

    /*
    *
    * 再来一单
    * */
    void repeat(Long id);

    /*
    *
    * 管理端搜索订单
    * */
    PageResult adminPageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /*
    * 各状态订单数目统计
    * */
    OrderStatisticsVO statics();

    /*
    *
    * 接单*/
    void confirm(Long id);

    /*
    * 拒单
    * */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /*
    * 管理端取消订单
    * */
    void admincancel(OrdersCancelDTO ordersCancelDTO);

    /*
    * 派送订单
    * */
    void delivery(Long id);

    /*
    * 完成订单
    * */
    void complete(Long id);

    /*
    * 客户催单
    * */
    void reminder(Long id);
}

