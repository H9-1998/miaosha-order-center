package com.miaosha.ordercenter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @auhor: dhz
 * @date: 2020/11/23 20:27
 * 购物车
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCartModel implements Serializable {
    // 对应的用户id
    private Integer userId;
    // 加购商品列表
    private List<ItemModel> items;
    // 总价
    private BigDecimal totalPrice;

}
