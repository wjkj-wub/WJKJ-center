package com.miqtech.master.dao.code;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.code.InviteActivity;

public interface InviteActivityDao extends JpaSpecificationExecutor<InviteActivity>,
		PagingAndSortingRepository<InviteActivity, Long> {
}
