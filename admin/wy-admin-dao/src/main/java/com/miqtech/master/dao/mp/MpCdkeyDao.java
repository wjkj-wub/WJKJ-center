package com.miqtech.master.dao.mp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mp.MpCdkey;

public interface MpCdkeyDao extends PagingAndSortingRepository<MpCdkey, Long>, JpaSpecificationExecutor<MpCdkey> {

	List<MpCdkey> findByOpenIdAndCategoryIdAndValid(String openId, Long categoryId, Integer valid);
}
