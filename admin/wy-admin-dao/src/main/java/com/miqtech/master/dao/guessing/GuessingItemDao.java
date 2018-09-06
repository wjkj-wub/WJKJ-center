package com.miqtech.master.dao.guessing;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.guessing.GuessingItem;

import java.util.List;

public interface GuessingItemDao
		extends PagingAndSortingRepository<GuessingItem, Long>, JpaSpecificationExecutor<GuessingItem> {

	GuessingItem findByIdAndValid(Long itemId, Integer valid);

	/**
	 * 获取所有的有效竞猜对象列表
	 * @param valid
	 * @return
	 */
	List<GuessingItem> findAllByValid(Integer valid);
}
