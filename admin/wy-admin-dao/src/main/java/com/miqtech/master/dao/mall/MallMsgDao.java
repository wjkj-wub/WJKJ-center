package com.miqtech.master.dao.mall;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.MallMsg;

public interface MallMsgDao extends JpaSpecificationExecutor<MallMsg>, PagingAndSortingRepository<MallMsg, Long> {

}
