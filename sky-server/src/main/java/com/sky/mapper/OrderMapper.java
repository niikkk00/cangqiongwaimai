package com.sky.mapper;

import org.apache.ibatis.annotations.Param;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.core.annotation.Order;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    Orders getByNumberAndUserId(@Param("number") String number,
                                @Param("userId") Long userId);

    int markPaid(@Param("number") String number,
                 @Param("userId") Long userId);
}
