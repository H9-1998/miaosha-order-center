package com.miaosha.ordercenter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @auhor: dhz
 * @date: 2020/11/16 00:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "order_info")
public class OrderInfo {
    // 订单id 16位 例: 2020111600000100
    @Id
    private String id;
    private Integer userId;
    private Integer itemId;
    private Integer amount;
    private Double itemPrice;
    private Double totalPrice;
    private Integer promoId;
}
