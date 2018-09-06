package com.miqtech.master.dao.activity;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityImg;

/**
 * 赛事图片DAO
 */
public interface ActivityImgDao extends PagingAndSortingRepository<ActivityImg, Long>,
		JpaSpecificationExecutor<ActivityImg> {
}