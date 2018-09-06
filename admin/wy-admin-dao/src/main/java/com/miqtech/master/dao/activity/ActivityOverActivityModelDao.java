package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityOverActivityModel;

/**
 * 赛事资讯模版操作DAO
 */
public interface ActivityOverActivityModelDao extends PagingAndSortingRepository<ActivityOverActivityModel, Long>,
		JpaSpecificationExecutor<ActivityOverActivityModel> {

	List<ActivityOverActivityModel> findByValid(Integer valid);

	List<ActivityOverActivityModel> findByInfoIdAndValid(long infoId, int valid);
}