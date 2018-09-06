package com.miqtech.master.dao.guessing;

import com.miqtech.master.entity.guessing.GuessingInfo;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * 竞猜 Dao
 * @author zhangyuqi
 * 2017年06月01日
 */
public interface GuessingInfoDao
		extends JpaSpecificationExecutor<GuessingInfo>, PagingAndSortingRepository<GuessingInfo, Long> {
	/**
	 * 根据ID查找有效竞猜
	 * @param id
	 * @param valid
	 * @return
	 */
	GuessingInfo findByIdAndValid(Long id, Integer valid);

	/**
	 * 查询所有有效竞猜
	 * @param valid
	 * @return
	 */
	List<GuessingInfo> findAllByValid(Integer valid);

}
