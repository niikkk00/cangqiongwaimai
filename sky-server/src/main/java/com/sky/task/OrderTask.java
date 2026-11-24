package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，定时处理订单状态
 */

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;
    /**
     * 处理超时订单的方法
     */
    @Scheduled(cron = "0 0/1 * * * ?")//每分钟执行一次
    //@Scheduled(cron = "1/5 * * * * ?")
    public void processTimeoutOrders() {
        LocalDateTime now = LocalDateTime.now();
        log.info("处理超时订单:{}", now);

        List<Orders> orders = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, now.minusMinutes(15));
        for (Orders order : orders) {
            Orders update = Orders.builder()
                    .id(order.getId())
                    .status(Orders.CANCELLED)
                    .cancelReason("订单超时未支付，系统自动取消")
                    .cancelTime(now)
                    .build();
            orderMapper.update(update, Orders.PENDING_PAYMENT);
        }
    }


    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨一点触发执行
    //@Scheduled(cron = "0/5 * * * * ?")
    public void processDeliveryOrders() {
        LocalDateTime now = LocalDateTime.now();
        log.info("处理配送订单:{}", now);

        // 沿用订单创建时间作为自动完成的时间条件。
        List<Orders> orders = orderMapper.getByStatusAndOrderTimeLT(
                Orders.DELIVERY_IN_PROGRESS, now.minusHours(1));
        for (Orders order : orders) {
            Orders update = Orders.builder()
                    .id(order.getId())
                    .status(Orders.COMPLETED)
                    .deliveryTime(now)
                    .build();
            orderMapper.update(update, Orders.DELIVERY_IN_PROGRESS);
        }
    }
}
