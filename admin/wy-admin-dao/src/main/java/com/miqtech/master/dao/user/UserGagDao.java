package com.miqtech.master.dao.user;

import com.miqtech.master.entity.user.UserGag;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * 禁言名单Dao
 */
public interface UserGagDao extends JpaSpecificationExecutor<UserGag>, PagingAndSortingRepository<UserGag, Long> {

	/**
	 * 查找所有
	 */
	@Query("select sa from UserGag sa where days<>-1")
	@Override
	List<UserGag> findAll();
}
