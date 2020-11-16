package com.miaosha.ordercenter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @auhor: dhz
 * @date: 2020/11/13 18:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemModel implements Serializable {
    private Integer id;
    private String title;
    private Double price;
    private String description;
    private Integer sales;
    private String imgUrl;
    private Integer stock;
    //使用聚合模型，如果promoModel不为空，表示有还未结束的秒杀活动
    private PromoModel promoModel;
}
