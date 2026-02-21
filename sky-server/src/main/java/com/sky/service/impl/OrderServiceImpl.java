package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    /*
    * 用户下单
    * */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理异常
        //地址为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //购物车为空
        Long currentId = BaseContext.getCurrentId();//用户id
        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(currentId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(cart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表插入数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(currentId);
        orderMapper.insert(orders);
        List<OrderDetail> orderDetailList = new ArrayList<>();
        //向订单明细表插入数据
        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());//设置当前订单明细相关联订单id
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        //清空购物车
        shoppingCartMapper.deleteByUserId(currentId);
        //封装VO返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().
                id(orders.getId()).
                orderTime(orders.getOrderTime()).
                orderNumber(orders.getNumber()).
                orderAmount(orders.getAmount()).
                build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        /*JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );*/
        //自己new一个jsonobject
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /*
    *
    * 查询历史订单
    *
    * */
    @Override
    public PageResult listHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(userId);
        //开启一页
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        List<Orders> allOrders = orderMapper.pageQuery(ordersPageQueryDTO);
        Page p =(Page) allOrders;
        long total = p.getTotal();
        List<OrderVO> list = new ArrayList<>();
        //查询订单明细，放入OrderVO
        if (allOrders !=null && allOrders.size()>0){
            for (Orders orders : allOrders) {
                Long orderId = orders.getId();
                //查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO =new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                orderVO.setOrderDetailList(orderDetails);
                list.add(orderVO);
            }
        }
        PageResult pageResult = new PageResult(total,list);
        return pageResult;
    }

    /*
    *
    * 查询订单明细
    * */
    @Override
    public OrderVO getOrderDetail(Long id) {

        //查询orders
        Orders order=orderMapper.getById(id);
        //查询order_detail
        List<OrderDetail> list=orderDetailMapper.getByOrderId(id);
        //封装OrderVO，返回
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(list);
        return orderVO;
    }
    /*
    *
    * 用户取消订单
    * */
    @Override
    public void userCancel(Long id) throws Exception {
        //查询订单是否存在
        Orders orderDB = orderMapper.getById(id);
        if (orderDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        //如果已经接单，无法退款
        if (orderDB.getStatus()>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(orderDB.getId());
        //如果是待接单，退款
        if (orderDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            //调用微信支付退款接口
            weChatPayUtil.refund(
                    orderDB.getNumber(), //商户订单号
                    orderDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额
            //设置退款状态
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }
    /*
    * 再来一单
    *
    * */
    @Override
    public void repeat(Long id) {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        //查询order_detail,把各数据装入购物车
        for (OrderDetail orderDetail : orderDetailMapper.getByOrderId(id)) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart,"id");
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setUserId(userId);
            shoppingCartMapper.insert(shoppingCart);
        }


    }


    /*
    * 管理端搜索订单
    *
    * */
    @Override
    public PageResult adminPageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        //开启一页
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        List<Orders> allOrders = orderMapper.adminpageQuery(ordersPageQueryDTO);
        Page p =(Page) allOrders;
        long total = p.getTotal();
        List<OrderVO> list = new ArrayList<>();
        //讲order基本数据喝orderDishes字段放入OrderVO,再把orderVO假如list中
        if (allOrders !=null && allOrders.size()>0){
            for (Orders orders : allOrders) {
                OrderVO orderVO =new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                orderVO.setOrderDishes(orderDishes(orders));
                list.add(orderVO);
            }
        }
        PageResult pageResult = new PageResult(total,list);
        return pageResult;
    }

    /*
    *
    * 根据orderid拼接orderDishes字段
    *
    * */
    private String orderDishes(Orders orders){
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }
}
