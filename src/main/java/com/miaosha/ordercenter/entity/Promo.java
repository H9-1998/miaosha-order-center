package com.miaosha.ordercenter.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @auhor: dhz
 * @date: 2020/11/13 19:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "promo")
public class Promo {
    // 活动id
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    // 商品id
    private Integer itemId;
    // 活动名称
    private String promoName;
    // 活动价格
    private Double promoItemPrice;
    // 活动开始时间
    private Date startTime;
    // 活动结束时间
    private Date endTime;
}
