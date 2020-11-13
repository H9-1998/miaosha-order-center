package com.miaosha.ordercenter.dao;

import com.miaosha.ordercenter.entity.Item;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * @auhor: dhz
 * @date: 2020/11/13 17:57
 */
@Repository
public interface ItemDao extends Mapper<Item> {
}
