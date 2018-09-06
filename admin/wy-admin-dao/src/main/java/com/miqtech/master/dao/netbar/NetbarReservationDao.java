package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarReservation;

/**
 * 网吧预定订单操作DAO
 */
public interface NetbarReservationDao extends PagingAndSortingRepository<NetbarReservation, Long>,
		JpaSpecificationExecutor<NetbarReservation> {
	/**
	 * 根据用户id查找预定订单信息(创建时间降序)
	 * @param userId 用户id
	 */
	List<NetbarReservation> findByUserIdOrderByCreateDateDesc(Long userId);
}