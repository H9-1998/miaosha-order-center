package com.miaosha.ordercenter.dao;

import com.miaosha.ordercenter.entity.StockLog;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * @auhor: dhz
 * @date: 2020/11/16 15:35
 */
@Repository
public interface StockLogDao extends Mapper<StockLog> {
}
