package com.miqtech.master.dao.audition;

import com.miqtech.master.entity.audition.AuditionAwardConfig;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * 海选赛-获胜奖励 配置 Dao
 * @author zhangyuqi
 * 2017年06月14日
 */
public interface AuditionAwardDao
		extends JpaSpecificationExecutor<AuditionAwardConfig>, PagingAndSortingRepository<AuditionAwardConfig, Long> {

	/**
	 * 根据有效性查询获胜奖励
	 */
	List<AuditionAwardConfig> findAllByValid(Integer valid);
}
