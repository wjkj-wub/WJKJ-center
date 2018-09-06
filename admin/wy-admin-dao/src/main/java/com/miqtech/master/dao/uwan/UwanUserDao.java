package com.miqtech.master.dao.uwan;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.uwan.UwanUser;

/**
 * 网吧pc客户端广告
 */
public interface UwanUserDao
		extends PagingAndSortingRepository<UwanUser, Long>, JpaSpecificationExecutor<UwanUser> {

	List<UwanUser> findByUwanUserIdInAndValid(List<String> uwanUserIds, Integer valid);
}