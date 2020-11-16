package com.miaosha.ordercenter.controller;

import com.miaosha.ordercenter.error.BusinessException;
import com.miaosha.ordercenter.error.EmBusinessError;
import com.miaosha.ordercenter.mq.MqProducer;
import com.miaosha.ordercenter.response.CommonReturnType;
import com.miaosha.ordercenter.service.ItemService;
import com.miaosha.ordercenter.service.OrderService;
import com.miaosha.ordercenter.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import java.util.concurrent.*;

/**
 * @auhor: dhz
 * @date: 2020/11/13 19:18
 */
@RestController
@RequestMapping("/order")
@Slf4j
@Api(tags = {"订单处理相关api"})
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MqProducer mqProducer;

    private ExecutorService executorService;

    @PostConstruct
    public void init(){
        executorService = Executors.newFixedThreadPool(20);
    }

    @PostMapping("create-order")
    @ApiOperation("创建订单")
    public CommonReturnType createOrder(@RequestParam("itemId") Integer itemId,
                                        @RequestParam("amount") Integer amount,
                                        @RequestParam("promoId") Integer promoId,
                                        @RequestParam("token") String token,
                                        @RequestParam("promoToken") String promoToken) throws BusinessException {

        // 从token取出用户id
        Integer userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null)
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);

        if (promoId != null){
            // 是秒杀商品, 判断秒杀令牌是否存在
            if (redisTemplate.opsForValue().get("promo_token_" + promoId + "_userId_" + userId + "_itemId_" + itemId) == null)
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀令牌不存在");
            String promoTokenInRedis = redisTemplate.opsForValue().get("promo_token_" + promoId + "_userId_" + userId + "_itemId_" + itemId).toString();
            if (!StringUtils.equals(promoToken, promoTokenInRedis)){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀令牌不存在");
            }
        }

        // 用线程池去队列化执行, 避免过多请求直接打崩下游
        Future<Object> future = executorService.submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                // 生成流水号
                String stockLogId = itemService.initStockLog(itemId, amount);

                // 事务性扣减库存, 先扣redis 成功后扣db, 失败则回补redis库存
                if (!mqProducer.transactionAsyncReduceStock(itemId, amount, userId, stockLogId, promoId))
                    throw new BusinessException(EmBusinessError.MQ_SEND_FAIL);
                return null;
            }
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(EmBusinessError.UNKNOW_ERROR);
        } catch (ExecutionException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(EmBusinessError.UNKNOW_ERROR);
        }



        return CommonReturnType.create(null);
    }


}
