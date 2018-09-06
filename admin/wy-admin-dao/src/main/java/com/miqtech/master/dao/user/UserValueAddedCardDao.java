package com.miqtech.master.dao.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.user.UserValueAddedCard;

public interface UserValueAddedCardDao
		extends PagingAndSortingRepository<UserValueAddedCard, Long>, JpaSpecificationExecutor<UserValueAddedCard> {

	List<UserValueAddedCard> findByIdIn(List<Long> ids);

}