package com.miqtech.master.dao.pc.netbar;

import com.miqtech.master.entity.pc.netbar.PcNetbarUserRetention;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PcNetbarUserRetentionDao extends PagingAndSortingRepository<PcNetbarUserRetention, Long>,
		JpaSpecificationExecutor<PcNetbarUserRetention> {

}
