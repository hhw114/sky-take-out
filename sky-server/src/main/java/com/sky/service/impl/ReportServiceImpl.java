package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /*
    * 营业额统计
    * */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //集合存放begin到end日期集合
        List<LocalDate> datelist=new ArrayList<>();
        datelist.add(begin);
        while(!begin.equals(end)){
            //日期+1
            begin=begin.plusDays(1);
            datelist.add(begin);
        }
        //日期字符串
        String dateString = StringUtils.join(datelist, ",");

        //遍历查询营业额
        List<Double> turnoverList=new ArrayList<>();
        for (LocalDate date :datelist) {
            //查询date日期已完成状态的金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            //调用查询
            Double turnover=orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        //营业额字符串
        String turnoverString = StringUtils.join(turnoverList, ",");
        return TurnoverReportVO.builder().dateList(dateString).turnoverList(turnoverString).build();
    }

    /*
    * 用户数据统计
    * */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //集合存放begin到end日期集合
        List<LocalDate> datelist=new ArrayList<>();
        datelist.add(begin);
        while(!begin.equals(end)){
            //日期+1
            begin=begin.plusDays(1);
            datelist.add(begin);
        }
        //日期字符串
        String dateString = StringUtils.join(datelist, ",");

        //每日总量用户查询,增量用户查询
        List<Integer> totalUserList=new ArrayList<>();
        List<Integer> newUserList=new ArrayList<>();
        for (LocalDate date :datelist) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //新增查询
            Map mapnew = new HashMap();
            mapnew.put("begin",beginTime);
            mapnew.put("end",endTime);
            Integer newUser = userMapper.countByMap(mapnew);
            newUserList.add(newUser);

            //总量查询
            Map maptotal = new HashMap();
            mapnew.put("end",endTime);
            Integer totalUser = userMapper.countByMap(maptotal);
            totalUserList.add(totalUser);
        }

        //新增字符串，日期字符串
        String newString = StringUtils.join(newUserList, ",");
        String totalString=StringUtils.join(totalUserList,",");
        return UserReportVO.builder().dateList(dateString).totalUserList(totalString).newUserList(newString).build();
    }
    //获得订单数据
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //集合存放begin到end日期集合
        List<LocalDate> datelist=new ArrayList<>();
        LocalDate current = begin;
        datelist.add(begin);
        while(!current.equals(end)){
            //日期+1
            current=current.plusDays(1);
            datelist.add(current);
        }
        //日期字符串
        String dateString = StringUtils.join(datelist, ",");
        //查询总订单数
        Map maptotal = new HashMap();
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        maptotal.put("begin",beginTime);
        maptotal.put("end",endTime);
        Integer totalOrderCount= orderMapper.countByMap(maptotal);
        //查询总有效订单数
        maptotal.put("status", Orders.COMPLETED);
        Integer  validOrderCount=orderMapper.countByMap(maptotal);

        //分别查询每日订单数
        List<Integer> orderList=new ArrayList<>();
        List<Integer> vaildOrderList=new ArrayList<>();
        for (LocalDate date :datelist) {
            LocalDateTime beginTime1 = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime1 = LocalDateTime.of(date, LocalTime.MAX);
            Map map1 = new HashMap();
            map1.put("begin",beginTime1);
            map1.put("end",endTime1);
            //查询总数
            Integer ordernum=orderMapper.countByMap(map1);
            //查询有效数字
            map1.put("status", Orders.COMPLETED);
            Integer vaildordernum=orderMapper.countByMap(map1);
            vaildOrderList.add(vaildordernum);
            orderList.add(ordernum);
        }
        //构建每日订单列表和有效订单列表字符串
        String validOrderCountList=StringUtils.join(vaildOrderList,",");
        String orderCountList=StringUtils.join(orderList,",");
        //计算总订单完成率
        Double completeRate= validOrderCount * 1.0/totalOrderCount;

        return OrderReportVO.builder().orderCountList(orderCountList).validOrderCountList(validOrderCountList).dateList(dateString).totalOrderCount(totalOrderCount).validOrderCount(validOrderCount).orderCompletionRate(completeRate).build();

    }
    /*
     * 查询top10热销商品
     * */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        //查询orders和order_detail表
        List<GoodsSalesDTO> salesTop10 = orderMapper.top10(beginTime, endTime);
        List<String> names = salesTop10.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.toList());

        List<Integer> nums = salesTop10.stream()
                .map(GoodsSalesDTO::getNumber)
                .collect(Collectors.toList());
        //构建namelist和numberlist字符串
        String nameList=StringUtils.join(names,",");
        String numberList=StringUtils.join(nums,",");


        return SalesTop10ReportVO.builder().nameList(nameList).numberList(numberList).build();
    }
}
