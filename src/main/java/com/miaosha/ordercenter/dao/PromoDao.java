package com.miaosha.ordercenter.dao;

import com.miaosha.ordercenter.entity.Promo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * @auhor: dhz
 * @date: 2020/11/13 19:33
 */
@Repository
public interface PromoDao extends Mapper<Promo> {

    /**
     * 用itemId取promo
     * @param itemId
     * @return
     */
    Promo selectPromoByItemId(@Param("itemId") Integer itemId);
}
