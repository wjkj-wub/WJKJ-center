package com.miqtech.master.dao.amuse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.amuse.AmuseVerifyImg;

/**
 * 娱乐赛审核图片Dao
 */
public interface AmuseVerifyImgDao extends PagingAndSortingRepository<AmuseVerifyImg, Long>,
		JpaSpecificationExecutor<AmuseVerifyImg> {

	List<AmuseVerifyImg> findByVerifyId(Long verifyId);

}
