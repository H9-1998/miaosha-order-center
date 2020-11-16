package com.miaosha.ordercenter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @auhor: dhz
 * @date: 2020/11/13 20:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromoModel {
    //秒杀活动的id
    private Integer id;

    //秒杀活动开启时间
    private Date startTime;

    //秒杀活动结束时间
    private Date endTime;

    //秒杀活动名
    private String promoName;

    //对应的商品id
    private Integer itemId;

    //商品的活动价
    private Double promoItemPrice;

    //秒杀活动状态，1为未开始，2为进行中，3为已结束
    private Integer promoStatus;
}
