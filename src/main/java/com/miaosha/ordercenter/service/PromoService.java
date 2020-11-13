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
import com.miaosha.ordercenter.model.PromoModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    /**
     * 生成秒杀令牌
     * @param itemId
     * @param promoId
     * @param userId
     * @return
     */
    public String generatePromoToken(Integer itemId, Integer promoId, Integer userId){
        // 判断商品是否已售罄
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)) {
            // 如果存在该key代表商品库存已空, 则不生成token
            return null;
        }

        // 查看活动状态
        Promo promo = promoDao.selectByPrimaryKey(promoId);
        PromoModel promoModel = convertPromoModelFromPromo(promo);
        if (promoModel == null)
            // 不存在该活动
            return null;

        ItemModel itemModel = itemService.getItemByItemIdInRedis(itemId);
        if (itemModel == null)
            // 商品不存在
            return null;

        if (promoModel.getStartTime().after(new Date())){
            promoModel.setPromoStatus(1);
        }else if (promoModel.getEndTime().before(new Date())){
            promoModel.setPromoStatus(3);
        }else {
            promoModel.setPromoStatus(2);
        }

        if (promoModel.getPromoStatus() != 2)
            // 活动未开始
            return null;

        //令牌数量-1，小于零后全部返回null
        long result = redisTemplate.opsForValue().increment("promo_token_count_"+promoId, -1);
        if (result < 0){
            return null;
        }

        //生成token并存入redis,有效时间为5分钟
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("promo_token_"+promoId+"_userId_"+userId+"_itemId_"+itemId, token, 5, TimeUnit.MINUTES);
        return token;
    }

//    ------------------------------------------非业务方法------------------------------------------------------------------

    /**
     * do装model
     * @param promo
     * @return
     */
    private PromoModel convertPromoModelFromPromo(Promo promo){
        if (promo == null)
            return null;
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promo, promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promo.getPromoItemPrice()));
        return promoModel;
    }
}
