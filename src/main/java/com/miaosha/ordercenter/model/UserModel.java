package com.miaosha.ordercenter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @auhor: dhz
 * @date: 2020/11/13 14:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserModel implements Serializable {
    private Integer id;

    private String name;

    private Byte gender;

    private Integer age;

    private String telephone;

    private String registerMode;

    private String thirdPartyId;

    private String encryptPassword;
}
