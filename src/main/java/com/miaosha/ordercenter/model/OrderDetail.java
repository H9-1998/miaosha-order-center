package com.miaosha.ordercenter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @auhor: dhz
 * @date: 2020/11/17 16:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetail {
    private String orderId;
    private Integer userId;
    private Integer itemId;
    private Integer amount;
    private Double itemPrice;
    private Double totalPrice;
    private Integer promoId;
    private String userName;
    private String telephone;
    private Byte gender;
}
