package com.miqtech.master.dao.finance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.finance.FinanceUser;

public interface FinanceUserDao extends PagingAndSortingRepository<FinanceUser, Long>,
		JpaSpecificationExecutor<FinanceUser> {

	FinanceUser findByUsernameAndPasswordAndValid(String username, String password, Integer valid);

	FinanceUser findByNicknameAndValid(String nickname, int valid);

	FinanceUser findByUsernameAndValid(String username, int valid);

	List<FinanceUser> findByAreaCodeAndValid(String areaCode, int valid);

}
