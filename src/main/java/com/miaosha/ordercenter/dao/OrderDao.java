package com.miaosha.ordercenter.dao;

import com.miaosha.ordercenter.entity.OrderInfo;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * @auhor: dhz
 * @date: 2020/11/16 00:57
 */
@Repository
public interface OrderDao extends Mapper<OrderInfo> {

}
