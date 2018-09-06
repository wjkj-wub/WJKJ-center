package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityOverActivityImg;

public interface ActivityOverActivityImgDao extends PagingAndSortingRepository<ActivityOverActivityImg, Long>,
		JpaSpecificationExecutor<ActivityOverActivityImg> {

	List<ActivityOverActivityImg> findByActivityIdAndValidOrderByCreateDateDesc(long id, int valid);

	List<ActivityOverActivityImg> findByActivityIdAndValidOrderByImgAsc(long id, int valid);

	List<ActivityOverActivityImg> findByIdIn(List<Long> ids);
}
