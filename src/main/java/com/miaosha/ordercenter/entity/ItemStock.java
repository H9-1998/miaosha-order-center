package com.miaosha.ordercenter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @auhor: dhz
 * @date: 2020/11/13 17:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "item_stock")
public class ItemStock {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private Integer stock;
    private Integer itemId;
}
