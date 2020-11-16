package com.miaosha.ordercenter.dao;

import com.miaosha.ordercenter.entity.OrderInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @auhor: dhz
 * @date: 2020/11/16 00:57
 */
@Repository
public interface OrderDao extends Mapper<OrderInfo> {

    /**
     * 获取某用户所有订单
     * @param userId
     * @return
     */
    List<OrderInfo> selectAllOrder(@Param("userId") Integer userId);

}
