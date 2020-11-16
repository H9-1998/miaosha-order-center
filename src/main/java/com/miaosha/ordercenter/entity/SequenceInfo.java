package com.miaosha.ordercenter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @auhor: dhz
 * @date: 2020/11/16 01:21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "sequence_info")
public class SequenceInfo {
    @Id
    private String name;
    private Integer currentValue;
    private Integer step;
}
