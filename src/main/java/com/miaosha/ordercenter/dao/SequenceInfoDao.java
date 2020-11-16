package com.miaosha.ordercenter.dao;

import com.miaosha.ordercenter.entity.SequenceInfo;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;


/**
 * @auhor: dhz
 * @date: 2020/11/16 01:22
 */
@Repository
public interface SequenceInfoDao extends Mapper<SequenceInfo> {
}
