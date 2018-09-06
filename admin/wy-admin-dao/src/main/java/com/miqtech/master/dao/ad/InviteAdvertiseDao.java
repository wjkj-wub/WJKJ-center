package com.miqtech.master.dao.ad;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.InviteAdvertise;

public interface InviteAdvertiseDao extends PagingAndSortingRepository<InviteAdvertise, Long>,
		JpaSpecificationExecutor<InviteAdvertise> {

	InviteAdvertise findByUserIdAndAdIdAndValid(long userId, long adId, int i);

}
