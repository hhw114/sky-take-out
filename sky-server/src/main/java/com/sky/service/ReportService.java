package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    /*
    * 统计时间区间内营业额
    * */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /*
    * 统计用户数据
    * */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /*
    统计订单数据
    * */
    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    /*
    * 查询top10热销商品
    * */
    SalesTop10ReportVO top10(LocalDate begin, LocalDate end);

    /*
    * 导出运营数据报表
    * */
    void exportBusinessData(HttpServletResponse response);
}
