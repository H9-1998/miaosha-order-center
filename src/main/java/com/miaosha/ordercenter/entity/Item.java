package com.miaosha.ordercenter.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * @auhor: dhz
 * @date: 2020/11/13 17:53
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "item")
public class Item implements Serializable {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private String title;
    private Double price;
    private String description;
    private Integer sales;
    private String imgUrl;
}
