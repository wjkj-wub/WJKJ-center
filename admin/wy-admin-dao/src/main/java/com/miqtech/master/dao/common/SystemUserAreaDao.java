package com.miqtech.master.dao.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.SysUserArea;

/**
 * 某个录入或者审核人员管辖地理信息操作DAO
 */
public interface SystemUserAreaDao
		extends PagingAndSortingRepository<SysUserArea, Long>, JpaSpecificationExecutor<SysUserArea> {

	/**
	 * 删除系统用户的关联地理信息
	 * @param id 录入或者审核用户id
	 */
	void deleteBySysUserId(Long id);

	List<SysUserArea> findBySysUserIdOrderByAreaIdAsc(Long userId);

	List<SysUserArea> findBySysUserId(Long id);
}