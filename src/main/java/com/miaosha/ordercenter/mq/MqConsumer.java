package com.miaosha.ordercenter.mq;

import com.alibaba.fastjson.JSON;
import com.miaosha.ordercenter.dao.StockLogDao;
import com.miaosha.ordercenter.entity.StockLog;
import com.miaosha.ordercenter.service.ItemService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @auhor: dhz
 * @date: 2020/11/15 22:42
 */
@Component
public class MqConsumer {

    private DefaultMQPushConsumer consumer;

    @Value("${mq.nameserver.addr}")
    private String nameServerAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private ItemService itemService;

    @Autowired
    private StockLogDao stockLogDao;

    @PostConstruct
    public void init() throws MQClientException {
        consumer = new DefaultMQPushConsumer("stock_consumer_group");
        consumer.setNamesrvAddr(nameServerAddr);
        consumer.subscribe(topicName, "*");

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                Message message = list.get(0);
                String jsonString = new String(message.getBody());
                Map<String, Object> bodyMap = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) bodyMap.get("itemId");
                Integer amount = (Integer) bodyMap.get("amount");
                String stockLogId = (String) bodyMap.get("stockLogId");
                StockLog stockLog = stockLogDao.selectByPrimaryKey(stockLogId);
                if (stockLog.getDbStatus() != 1)
                    // 不为1表示该订单已被处理过, 不再消费消息, 直接返回
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                // 扣减db库存
                itemService.decreaseDBStock(itemId, amount);
                stockLog.setDbStatus(2);
                stockLogDao.updateByPrimaryKey(stockLog);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

            }
        });

        consumer.start();
    }
}
