package com.miqtech.master.dao.guessing;

import com.miqtech.master.entity.guessing.GuessingInfoItem;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 竞猜对象与竞猜关系 Dao
 * @author zhangyuqi
 * 2017年06月01日
 */
public interface GuessingInfoItemDao
		extends JpaSpecificationExecutor<GuessingInfoItem>, PagingAndSortingRepository<GuessingInfoItem, Long> {
	/**
	 * 根据ID查找有效竞猜对象与竞猜对应关系
	 */
	GuessingInfoItem findByIdAndValid(Long id, Integer valid);

	/**
	 * 根据竞猜ID和竞猜对象位置查找有效竞猜对象与竞猜对应关系
	 */
	GuessingInfoItem findByGuessingInfoIdAndPositionAndValid(Long guessingInfoId, Integer position, Integer valid);
}
