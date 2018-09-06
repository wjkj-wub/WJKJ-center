package com.miqtech.master.dao.mall;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.MallTask;

/**
 * 商城任务DAO
 */
public interface MallTaskDao extends PagingAndSortingRepository<MallTask, Long>, JpaSpecificationExecutor<MallTask> {

	public MallTask findByIdAndValid(long id, int valid);

	public List<MallTask> findByTypeAndIdentify(int type, int identify);
}
