package com.miqtech.master.dao.msg;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.msg.CompetitionMsg;

/**
 * 赛事消息推送 接口
 *
 * @author gaohanlin
 * @create 2017年09月02日
 */
public interface CompetitionMsgDao
		extends JpaSpecificationExecutor<CompetitionMsg>, PagingAndSortingRepository<CompetitionMsg, Long> {

}
