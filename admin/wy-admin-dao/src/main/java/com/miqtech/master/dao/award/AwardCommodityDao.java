package com.miqtech.master.dao.award;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.miqtech.master.entity.award.AwardCommodity;

public interface AwardCommodityDao extends PagingAndSortingRepository<AwardCommodity, Long>,
		JpaSpecificationExecutor<AwardCommodity> {

	/**
	 * 查询某种库存下有效的商品
	 */
	@Query("select ac from AwardCommodity ac where NOW() >= startTime AND NOW() < endTime AND isUsed != 1 AND valid = 1 AND inventoryId = :inventoryId")
	public List<AwardCommodity> findUsefullyByInventoryId(@Param("inventoryId") Long inventoryId);
}
