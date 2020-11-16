package com.miaosha.ordercenter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @auhor: dhz
 * @date: 2020/11/16 15:31
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stock_log")
public class StockLog {
    @Id
    private String stockLogId;
    private Integer itemId;
    private Integer amount;
    private Integer status;
}
