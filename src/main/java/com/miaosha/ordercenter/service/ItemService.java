package com.miaosha.ordercenter.service;

import com.miaosha.ordercenter.dao.ItemDao;
import com.miaosha.ordercenter.dao.ItemStockDao;
import com.miaosha.ordercenter.entity.Item;
import com.miaosha.ordercenter.entity.ItemStock;
import com.miaosha.ordercenter.entity.StockLog;
import com.miaosha.ordercenter.error.BusinessException;
import com.miaosha.ordercenter.error.EmBusinessError;
import com.miaosha.ordercenter.model.ItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @auhor: dhz
 * @date: 2020/11/13 17:59
 */
@Service
public class ItemService {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemStockDao itemStockDao;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取所有商品
     * @return
     */
    public List<ItemModel> listItem(){
        List<ItemModel> itemModels = new ArrayList<>();
        List<Item> items = itemDao.selectAll();
        items.stream().forEach( item -> {
            ItemStock itemStock = itemStockDao.selectByItemId(item.getId());
            itemModels.add(convertFromDataObject(item, itemStock));
        });
        return itemModels;
    }

    /**
     * 从redis中取商品信息, 没有再从db中取
     * @param itemId
     * @return
     */
    public ItemModel getItemByItemIdInRedis(Integer itemId){
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_" + itemId);
        if (itemModel == null){
            // redis中没有再查数据库
            Item item = itemDao.selectByPrimaryKey(itemId);
            ItemStock itemStock = itemStockDao.selectByItemId(itemId);
            itemModel = convertFromDataObject(item, itemStock);
            // 存入redis
            redisTemplate.opsForValue().set("item_" + itemId, itemModel, 10, TimeUnit.MINUTES);
        }
        return itemModel;
    }

    /**
     * 从db中获取商品
     * @param itemId
     * @return
     * @throws BusinessException
     */
    public ItemModel getItemByItemIdInDB(Integer itemId) throws BusinessException {
        Item item = itemDao.selectByPrimaryKey(itemId);
        if (item == null)
            throw new BusinessException(EmBusinessError.ITEM_NOT_EXIST);

        ItemStock itemStock = itemStockDao.selectByItemId(itemId);
        ItemModel itemModel = convertFromDataObject(item, itemStock);
        return itemModel;

    }

    /**
     * 扣减redis中库存
     * @param itemId
     * @param amount
     * @return
     */
    public boolean decreaseRedisStock(Integer itemId, Integer amount){
        // 扣减redis中库存
        Long res = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue() * -1);
        if (res > 0){
            // 扣减后剩余量大于0直接返回
            return true;
        } else if (res == 0){
            // 等于0给该商品打上售罄标识
            redisTemplate.opsForValue().set("promo_item_stock_invalid_" + itemId, "true");
            return true;
        } else{
            // 小于0应该失败, 因为小于0代表库存不足本次扣减, 回补redis库存
            increaseStock(itemId, amount);
            return false;
        }
    }

    /**
     * 扣减数据库库存
     * @param itemId
     * @param amount
     * @return
     */
    @Transactional
    public boolean decreaseDBStock(Integer itemId, Integer amount){
        ItemStock itemStock = itemStockDao.selectByItemId(itemId);
        Integer stock = itemStock.getStock();
        if (stock < amount)
            // 库存不足 返回false
            return false;

        itemStock.setStock(stock-amount);
        itemStockDao.updateByPrimaryKey(itemStock);
        return true;
    }

    /**
     * 回滚商品库存(redis)
     * @param itemId
     * @param amount
     * @return
     */
    @Transactional
    public boolean increaseStock(Integer itemId, Integer amount){
        redisTemplate.opsForValue().increment(itemId, amount);
        return true;
    }

    /**
     * 初始化流水号
     * @param itemId
     * @param amount
     * @return
     */
    @Transactional
    public String initStockLog(Integer itemId, Integer amount){
        String stockLogId = UUID.randomUUID().toString().replace("-", "");
        StockLog stockLog = StockLog.builder()
                .itemId(itemId)
                .amount(amount)
                .status(1)
                .stockLogId(stockLogId)
                .build();
        return stockLogId;
    }




//    ----------------------------------------------非业务方法--------------------------------------------------


    /**
     * do转model
     * @param item
     * @param itemStock
     * @return
     */
    private ItemModel convertFromDataObject(Item item, ItemStock itemStock){
        if (item == null || itemStock == null)
            return null;

        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(item, itemModel);
        BeanUtils.copyProperties(itemStock, itemModel);
        return itemModel;
    }

}
