package com.miqtech.master.dao.netbar;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarQuotaConfig;

public interface NetbarQuotaConfigDao
		extends JpaSpecificationExecutor<NetbarQuotaConfig>, PagingAndSortingRepository<NetbarQuotaConfig, Long> {

	List<NetbarQuotaConfig> findByNetbarIdOrderByMonthlyDesc(Long netbarId);

	List<NetbarQuotaConfig> findByNetbarIdAndMonthly(Long netbarId, Date monthly);

}
