package com.miqtech.master.dao.code;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.code.InviteCode;

public interface InviteCodeDao extends JpaSpecificationExecutor<InviteCode>,
		PagingAndSortingRepository<InviteCode, Long> {
	InviteCode findByUserId(Long userId);

	InviteCode findByCodeAndValid(String code, int valid);

	InviteCode findByCode(String code);

}
