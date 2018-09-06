package com.miqtech.master.dao.mall;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.MallCdkey;

public interface MallCdkeyDao extends JpaSpecificationExecutor<MallCdkey>, PagingAndSortingRepository<MallCdkey, Long> {
	List<MallCdkey> findByItemIdAndIsUseNot(long itemId, Integer isUse);

	List<MallCdkey> findByHistoryIdAndIsUse(Long historyId, Integer isUse);

	List<MallCdkey> findByStockIdAndIsUse(Long stockId, Integer isUse);

}
