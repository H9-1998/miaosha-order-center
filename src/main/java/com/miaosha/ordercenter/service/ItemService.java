package com.miaosha.ordercenter.service;

import com.miaosha.ordercenter.dao.ItemDao;
import com.miaosha.ordercenter.dao.ItemStockDao;
import com.miaosha.ordercenter.dao.PromoDao;
import com.miaosha.ordercenter.dao.StockLogDao;
import com.miaosha.ordercenter.entity.*;
import com.miaosha.ordercenter.error.BusinessException;
import com.miaosha.ordercenter.error.EmBusinessError;
import com.miaosha.ordercenter.model.ItemModel;
import com.miaosha.ordercenter.model.PromoModel;
import com.miaosha.ordercenter.model.ShoppingCartModel;
import com.miaosha.ordercenter.response.CommonReturnType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @auhor: dhz
 * @date: 2020/11/13 17:59
 */
@Slf4j
@Service
public class ItemService {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemStockDao itemStockDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PromoDao promoDao;

    @Autowired
    private StockLogDao stockLogDao;

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
    public ItemModel getItemByItemIdInRedis(Integer itemId) throws BusinessException {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_" + itemId);
        if (itemModel == null){
            // redis中没有再查数据库
            itemModel = getItemFromDB(itemId);
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
    public ItemModel getItemFromDB(Integer itemId) throws BusinessException {
        Item item = itemDao.selectByPrimaryKey(itemId);
        if (item == null)
            throw new BusinessException(EmBusinessError.ITEM_NOT_EXIST);

        ItemStock itemStock = itemStockDao.selectByItemId(itemId);
        ItemModel itemModel = convertFromDataObject(item, itemStock);

        // 查询是否为活动商品 是的话将活动信息设置进itemModel
        Promo promo = promoDao.selectPromoByItemId(itemId);
        if (promo != null){
            PromoModel promoModel = convertPromoModelFromDataObject(promo);
            itemModel.setPromoModel(promoModel);
        }
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
                .redisStatus(1)
                .dbStatus(1)
                .stockLogId(stockLogId)
                .build();
        stockLogDao.insertSelective(stockLog);
        return stockLogId;
    }

    /**
     * 将商品加进购物车
     * @param itemId    商品id
     * @param amount    加购数量
     * @param userId    用户id
     * @return
     */
    @Transactional
    public boolean addItemIntoShoppingCart(Integer itemId, Integer amount, Integer userId){
        try {
            // 1, 先判断商品是否存在
            ItemModel itemModel = this.getItemByItemIdInRedis(itemId);
            if (itemModel == null){
                // 不存在该商品
                throw new BusinessException(EmBusinessError.ITEM_NOT_EXIST);
            }
            // 2, 从redis中取出该用户的购物车, 有则添加, 没有则初始化购物车
            ShoppingCartModel shoppingCartModel = (ShoppingCartModel) redisTemplate.opsForValue().get("shopping_cart_userId_" + userId);
            if (shoppingCartModel == null){
                // 没有该用户的购物车, 进行初始化购物车, 同时初始化加购商品列表
                shoppingCartModel = new ShoppingCartModel();
                shoppingCartModel.setUserId(userId);
                // 用LinkedList, 因为插入删除操作较多
                List<ItemModel> list = new LinkedList<>();
                shoppingCartModel.setItems(list);
            }
            // 2, 取出购物车中的商品列表, 判断该商品是否已在购物车中, 如果已存在直接加数量
            List<ItemModel> items = shoppingCartModel.getItems();
            // 用来判断购物车中是否有该商品, 默认为false
            AtomicReference<Boolean> isInCart = new AtomicReference<>(false);
            items.stream().forEach(item -> {
                if (item.getId() == itemId){
                    item.setAmount(item.getAmount() + amount);
                    isInCart.set(true);
                }
            });
            // 购物车中未有该商品, 将其设置数量并存进购物车中
            if (isInCart.get() == false){
                itemModel.setAmount(amount);
                items.add(itemModel);
            }
            // 将加购商品列表存进购物车中
            shoppingCartModel.setItems(items);

            // 3, 购物车存回redis
            redisTemplate.opsForValue().set("shopping_cart_userId_" + userId, shoppingCartModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 查询某个用户的购物车
     * @param userId    用户id
     * @return
     */
    public CommonReturnType getShoppingCartByUserId(Integer userId){
        ShoppingCartModel shoppingCartModel = (ShoppingCartModel) redisTemplate.opsForValue().get("shopping_cart_userId_" + userId);
        shoppingCartModel.setTotalPrice(calculateTotalPrice(shoppingCartModel));
        return CommonReturnType.create(shoppingCartModel);
    }

    /**
     * 清空购物车
     * @param userId
     * @return
     */
    public CommonReturnType cleanShoppingCart(Integer userId){
        ShoppingCartModel shoppingCartModel = (ShoppingCartModel) redisTemplate.opsForValue().get("shopping_cart_userId_" + userId);
        List<ItemModel> items = new ArrayList<>();
        shoppingCartModel.setItems(items);
        redisTemplate.opsForValue().set("shopping_cart_userId_" + userId, shoppingCartModel);
        return CommonReturnType.create(null);
    }

    /**
     * 结算购物车总价
     * @param cart
     * @return
     */
    public BigDecimal calculateTotalPrice(ShoppingCartModel cart){
        if (cart == null || cart.getItems() == null)
            // 购物车或商品列表为空, 直接返回0
            return new BigDecimal(0);

        AtomicReference<BigDecimal> totalPrice = new AtomicReference<BigDecimal>(new BigDecimal(0));
        List<ItemModel> items = cart.getItems();
        items.forEach( itemModel -> {
            if (itemModel.getPromoModel() == null || itemModel.getPromoModel().getPromoStatus() == null){
                // 不是活动商品, 直接计算平价 * 数量
                BigDecimal price = new BigDecimal(itemModel.getPrice()).multiply(new BigDecimal(itemModel.getAmount()));
                totalPrice.set(totalPrice.get().add(price));
            }
            else {
                // 活动商品, 查看活动状态 1未开始 2进行中 3已结束
                Integer promoStatus = itemModel.getPromoModel().getPromoStatus();
                if (promoStatus != 2){
                    // 活动进行中, 按活动价格计算
                    BigDecimal price = new BigDecimal(itemModel.getPromoModel().getPromoItemPrice()).multiply(new BigDecimal(itemModel.getAmount()));
                    totalPrice.set(totalPrice.get().add(price));
                }
                else{
                    // 活动未开始或结束, 按平价计算
                    BigDecimal price = new BigDecimal(itemModel.getPrice()).multiply(new BigDecimal(itemModel.getAmount()));
                    totalPrice.set(totalPrice.get().add(price));
                }
            }
        });
        return totalPrice.get();
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

    /**
     * promo装model
     * @param promo
     * @return
     */
    private PromoModel convertPromoModelFromDataObject(Promo promo){
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promo, promoModel);
        return promoModel;
    }

}
