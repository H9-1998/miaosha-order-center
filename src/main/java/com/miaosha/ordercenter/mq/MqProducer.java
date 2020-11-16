package com.miaosha.ordercenter.mq;


import com.alibaba.fastjson.JSON;
import com.miaosha.ordercenter.dao.StockLogDao;
import com.miaosha.ordercenter.entity.StockLog;
import com.miaosha.ordercenter.error.BusinessException;
import com.miaosha.ordercenter.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @auhor: dhz
 * @date: 2020/11/15 20:56
 */
@Component
@Slf4j
public class MqProducer {

    private TransactionMQProducer transactionMQProducer;

    @Value("${mq.nameserver.addr}")
    private String nameServerAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StockLogDao stockLogDao;


    @PostConstruct
    public void init() throws MQClientException {
        transactionMQProducer = new TransactionMQProducer("transaction_produce_group");
        transactionMQProducer.setNamesrvAddr(nameServerAddr);
        transactionMQProducer.start();

        transactionMQProducer.setTransactionListener(new TransactionListener() {
            /**
             * 发送消息后执行本地事务方法, 根据本地事务执行情况返回state, state为commit的消息才可以被消费
             * @param message
             * @param o
             * @return
             */
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                Integer itemId = (Integer) ((Map)o).get("itemId");
                Integer userId = (Integer) ((Map)o).get("userId");
                Integer promoId = (Integer) ((Map)o).get("promoId");
                String stockLogId = (String) ((Map)o).get("stockLogId");
                Integer amount = (Integer) ((Map)o).get("amount");
                // 本地事务扣减redis库存, 成功后再将消息设置成可消费给消费者去扣减数据库库存
                try {
                    orderService.createOrder(userId, itemId, promoId, amount, stockLogId);
                } catch (BusinessException e) {
                    log.error(e.getMessage(), e);
                    // 发生异常, 消息回滚, 流水单号设置为回滚(3)
                    StockLog stockLog = stockLogDao.selectByPrimaryKey(stockLogId);
                    stockLog.setStatus(3);
                    stockLogDao.updateByPrimaryKey(stockLog);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                // 正常返回commit 将消息设置为可消费
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            /**
             * 若本地事务方法过久没有返回, 会执行该方法去检查本地事务的执行情况
             * @param messageExt
             * @return
             */
            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                // 取出订单流水号
                String jsonString = messageExt.getBody().toString();
                Map<String, Object> map = JSON.parseObject(jsonString, Map.class);
                String stockLogId = map.get("stockLogId").toString();
                StockLog stockLog = stockLogDao.selectByPrimaryKey(stockLogId);

                if (stockLog == null)
                    // 可能订单未创建完成, 设为unknow等待下一次查询
                    return LocalTransactionState.UNKNOW;
                if (stockLog.getStatus() == 1)
                    // 仍未初始化状态, 等待下一次查询
                    return LocalTransactionState.UNKNOW;
                else if (stockLog.getStatus() == 2)
                    // 订单已创建成功, 返回commit
                    return LocalTransactionState.COMMIT_MESSAGE;
                else
                    // 订单创建失败, 回滚消息
                    return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        });
    }

    /**
     * 发送事务型消息扣减库存
     * @param itemId        商品id
     * @param amount        扣减数量
     * @param userId        用户id
     * @param stockLogId    订单流水号
     * @param promoId       活动id
     * @return
     */
    public boolean transactionAsyncReduceStock(Integer itemId, Integer amount, Integer userId, String stockLogId, Integer promoId){
        Map<String, Object> message = new HashMap<>();
        message.put("itemId", itemId);
        message.put("amount", amount);
        message.put("stockLogId", stockLogId);

        Map<String, Object> args = new HashMap<>();
        args.put("userId", userId);
        args.put("itemId", itemId);
        args.put("amount", amount);
        args.put("promoId", promoId);
        args.put("stockLogId", stockLogId);
        Message msg = new Message(topicName, "increase", JSON.toJSON(message).toString().getBytes(Charset.forName("utf-8")));
        TransactionSendResult transactionSendResult = null;
        try {
            transactionSendResult = transactionMQProducer.sendMessageInTransaction(msg, args);
        } catch (MQClientException e) {
            log.error(e.getMessage(), e);
            return false;
        }
        if (transactionSendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE){
            // 消息被成功消费, 返回true
            return true;
        }else if (transactionSendResult.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE){
            // 消息回滚, 返回false
            return false;
        }else {
            // 其他情况, 非commit都返回false
            return false;
        }
    }


}
