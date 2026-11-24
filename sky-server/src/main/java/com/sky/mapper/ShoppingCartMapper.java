package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 动态条件查询
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据id更新数量
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 插入购物车
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (dish_flavor,user_id, dish_id, setmeal_id, name, image, amount, number, create_time) " +
            "values (#{dishFlavor},#{userId}, #{dishId}, #{setmealId}, #{name}, #{image}, #{amount}, #{number}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户id删除购物车
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /**
     * 批量插入购物车数据
     *
     * @param shoppingCartList
     */
    void insertBatch(@Param("shoppingCartList") List<ShoppingCart> shoppingCartList);
}
