package com.miqtech.master.dao.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.user.UserComplaint;

public interface UserComplaintDao extends PagingAndSortingRepository<UserComplaint, Long>,
		JpaSpecificationExecutor<UserComplaint> {
	List<UserComplaint> findByUserIdAndSubIdAndValid(Long userId, Long subId, Integer valid);
}