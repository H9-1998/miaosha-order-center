package com.miaosha.ordercenter.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Ordering;
import com.miaosha.ordercenter.dao.OrderDao;
import com.miaosha.ordercenter.dao.SequenceInfoDao;
import com.miaosha.ordercenter.dao.StockLogDao;
import com.miaosha.ordercenter.entity.OrderInfo;
import com.miaosha.ordercenter.entity.SequenceInfo;
import com.miaosha.ordercenter.entity.StockLog;
import com.miaosha.ordercenter.entity.UserInfo;
import com.miaosha.ordercenter.error.BusinessException;
import com.miaosha.ordercenter.error.EmBusinessError;
import com.miaosha.ordercenter.feignClient.UserCenterFeignClient;
import com.miaosha.ordercenter.model.ItemModel;
import com.miaosha.ordercenter.model.OrderDetail;
import com.miaosha.ordercenter.response.CommonReturnType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


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

    @Autowired
    private UserCenterFeignClient userCenterFeignClient;

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
     * 获取某用户所有订单
     * @param userId
     * @return
     */
    public CommonReturnType selectAllOrder(Integer userId){
        List<OrderInfo> orderInfos = orderDao.selectAllOrder(userId);
        return CommonReturnType.create(orderInfos);
    }

    /**
     * 获取订单详情
     * @param orderId
     * @param token
     * @return
     */
    public CommonReturnType getOrderDetail(String token, String orderId) throws BusinessException {
        // 查询订单基本信息
        OrderInfo orderInfo = orderDao.selectByPrimaryKey(orderId);
        // 调用用户中心获取用户信息
        CommonReturnType res = userCenterFeignClient.getUserInfo(token);
        // 利用ObjectMapper将linkedHashMap转为UserInfo实体类
        ObjectMapper mapper = new ObjectMapper();
        UserInfo userInfo = mapper.convertValue(res.getData(), UserInfo.class);
        OrderDetail orderDetail = convertOrderDetail(userInfo, orderInfo);
        if (orderDetail == null)
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        return CommonReturnType.create(orderDetail);
    }

    /**
     * 生成订单序号
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



//    ----------------------------------------非业务方法-----------------------------------------------------------------------

    /**
     * 转换OrderDetail
     * @param userInfo
     * @param orderInfo
     * @return
     */
    public OrderDetail convertOrderDetail(UserInfo userInfo, OrderInfo orderInfo){
        if (orderInfo == null || userInfo == null)
            return null;

        OrderDetail orderDetail = new OrderDetail();
        BeanUtils.copyProperties(userInfo, orderDetail);
        BeanUtils.copyProperties(orderInfo, orderDetail);

        orderDetail.setOrderId(orderInfo.getId());
        orderDetail.setUserId(userInfo.getId());
        orderDetail.setUserName(userInfo.getName());
        return orderDetail;
    }
}
