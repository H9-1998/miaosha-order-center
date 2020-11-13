package com.miaosha.ordercenter.service;

import com.miaosha.ordercenter.dao.ItemDao;
import com.miaosha.ordercenter.dao.ItemStockDao;
import com.miaosha.ordercenter.dao.PromoDao;
import com.miaosha.ordercenter.entity.Item;
import com.miaosha.ordercenter.entity.ItemStock;
import com.miaosha.ordercenter.entity.Promo;
import com.miaosha.ordercenter.error.BusinessException;
import com.miaosha.ordercenter.error.EmBusinessError;
import com.miaosha.ordercenter.model.ItemModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @auhor: dhz
 * @date: 2020/11/13 19:27
 */
@Service
public class PromoService {

    @Autowired
    private PromoDao promoDao;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发布秒杀活动
     * @param promoId
     * @throws BusinessException
     */
    public void  publishPromo(Integer promoId) throws BusinessException {
        Promo promo = promoDao.selectByPrimaryKey(promoId);
        if (promo == null)
            // 活动不存在
            throw new BusinessException(EmBusinessError.PROMO_NOT_EXIST);

        // 活动存在, 取商品信息
        ItemModel itemModel = itemService.getItemByItemIdInDB(promo.getItemId());
        if (itemModel == null)
            throw new BusinessException(EmBusinessError.ITEM_NOT_EXIST);

        // 将活动商品库存存入redis
        redisTemplate.opsForValue().set("promo_item_stock_" + itemModel.getId(), itemModel.getStock());
        // 初始化秒杀令牌数量 商品库存*5
        redisTemplate.opsForValue().set("promo_token_count_" + promoId, itemModel.getStock()*5);
        return;
    }

}
