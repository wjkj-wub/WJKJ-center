package com.miqtech.master.dao.boon;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.boon.BoonCdkey;

public interface BoonCdkeyDao extends PagingAndSortingRepository<BoonCdkey, Long>, JpaSpecificationExecutor<BoonCdkey> {

	BoonCdkey findByCdkey(String cdkey);

	List<BoonCdkey> findByUserId(Long userId);

	List<BoonCdkey> findByUserIdAndProduction(Long uesrId, String production);

	List<BoonCdkey> findByProduction(String production);

	List<BoonCdkey> findByTypeAndExpiredDateGreaterThanAndUserIdIsNullAndUsedDateIsNull(Integer type, Date expiredDate);
}
