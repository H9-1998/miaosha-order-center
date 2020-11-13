package com.miaosha.ordercenter.dao;

import com.miaosha.ordercenter.entity.ItemStock;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * @auhor: dhz
 * @date: 2020/11/13 17:58
 */
@Repository
public interface ItemStockDao extends Mapper<ItemStock> {

    /**
     * 根据商品id查库存
     * @param itemId
     * @return
     */
    ItemStock selectByItemId(@Param("itemId") Integer itemId);
}
