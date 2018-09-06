package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityOverActivityModule;

public interface ActivityOverActivityModuleDao extends PagingAndSortingRepository<ActivityOverActivityModule, Long>,
		JpaSpecificationExecutor<ActivityOverActivityModule> {

	List<ActivityOverActivityModule> findByPidAndValid(long pid, int valid);

	List<ActivityOverActivityModule> findByValid(Integer valid);

	List<ActivityOverActivityModule> findByTypeAndPidAndValid(int type, long pid, int valid);

	List<ActivityOverActivityModule> findByPidInAndValid(List<Long> pid, int valid);

	List<ActivityOverActivityModule> findByPidAndOrderNumAndValid(Long pid, Integer orderNum, Integer valid);
}
