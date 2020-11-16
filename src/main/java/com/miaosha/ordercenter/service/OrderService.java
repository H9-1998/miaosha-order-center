package com.miaosha.ordercenter.service;

import com.miaosha.ordercenter.dao.OrderDao;
import com.miaosha.ordercenter.dao.SequenceInfoDao;
import com.miaosha.ordercenter.dao.StockLogDao;
import com.miaosha.ordercenter.entity.OrderInfo;
import com.miaosha.ordercenter.entity.SequenceInfo;
import com.miaosha.ordercenter.entity.StockLog;
import com.miaosha.ordercenter.error.BusinessException;
import com.miaosha.ordercenter.error.EmBusinessError;
import com.miaosha.ordercenter.model.ItemModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @auhor: dhz
 * @date: 2020/11/13 19:19
 */
@Service
public class OrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ItemService itemService;

    @Autowired
    private SequenceInfoDao sequenceInfoDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private StockLogDao stockLogDao;

    /**
     * 创建订单
     * @param userId
     * @param itemId
     * @param promoId
     * @param amount
     * @param stockLogId
     */
    @Transactional
    public OrderInfo createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws BusinessException {

        // 从redis中取商品, 没有再从db中取
        ItemModel itemModel = itemService.getItemByItemIdInRedis(itemId);
        if (itemModel == null)
            // 商品不存在
            throw new BusinessException(EmBusinessError.ITEM_NOT_EXIST);

        // 扣减redis库存, 该函数中有判断剩余库存是否足够扣减
        boolean res = itemService.decreaseRedisStock(itemId, amount);
        if (res == false)
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);

        // 创建订单基本信息
        OrderInfo orderInfo = OrderInfo.builder()
                .id(generateOrderNo())
                .userId(userId)
                .itemId(itemId)
                .amount(amount)
                .build();

        // 若为活动商品, 设置活动id, 活动价格
        if (promoId != null){
            orderInfo.setPromoId(promoId);
            orderInfo.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }

        // 平价商品, 设置原价格
        if (promoId == null){
            orderInfo.setItemPrice(itemModel.getPrice());
        }

        // 设置总价
        orderInfo.setTotalPrice(orderInfo.getItemPrice() * orderInfo.getAmount());

        // 查找流水单号, 并设置为成功状态
        StockLog stockLog = stockLogDao.selectByPrimaryKey(stockLogId);
        if (stockLog == null)
            throw new BusinessException(EmBusinessError.UNKNOW_ERROR);
        // 1初始化 2成功 3失败
        stockLog.setRedisStatus(2);

        // 更新db
        stockLogDao.updateByPrimaryKey(stockLog);
        orderDao.insertSelective(orderInfo);
        return orderInfo;

    }

    /**
     * 获取订单序号
     * @return
     */
    @Transactional
    public String generateOrderNo(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        String nowDateStr = simpleDateFormat.format(date);
        SequenceInfo orderInfo = sequenceInfoDao.selectByPrimaryKey("order_info");
        StringBuilder seq = new StringBuilder();
        seq.append(nowDateStr);
        seq.append(orderInfo.getCurrentValue());
        orderInfo.setCurrentValue(orderInfo.getCurrentValue()+orderInfo.getStep());
        sequenceInfoDao.updateByPrimaryKey(orderInfo);
        return seq.toString();
    }
}
