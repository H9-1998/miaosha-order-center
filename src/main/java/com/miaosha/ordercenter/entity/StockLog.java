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
    // 扣redis库存状态 1初始化 2成功 3回滚
    private Integer redisStatus;
    // 扣db库存状态 1初始化 2成功 3回滚
    private Integer dbStatus;
}
