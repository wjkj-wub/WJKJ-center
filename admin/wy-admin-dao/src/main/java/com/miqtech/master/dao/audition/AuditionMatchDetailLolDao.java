package com.miqtech.master.dao.audition;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.audition.AuditionMatchDetailLol;

public interface AuditionMatchDetailLolDao extends JpaSpecificationExecutor<AuditionMatchDetailLol>,
		PagingAndSortingRepository<AuditionMatchDetailLol, Long> {

}
