package com.miqtech.master.dao.pc.detail;

import com.miqtech.master.entity.pc.detail.CoinDetail;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 积分记录 DAO
 *
 * @author zhangyuqi
 * @create 2017年09月08日
 */
public interface CoinDetailDao
		extends JpaSpecificationExecutor<CoinDetail>, PagingAndSortingRepository<CoinDetail, Long> {

}
