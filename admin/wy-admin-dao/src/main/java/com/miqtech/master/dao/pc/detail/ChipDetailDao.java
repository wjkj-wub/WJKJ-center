package com.miqtech.master.dao.pc.detail;

import com.miqtech.master.entity.pc.detail.ChipDetail;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 娱币记录 DAO
 *
 * @author zhangyuqi
 * @create 2017年09月08日
 */
public interface ChipDetailDao
		extends JpaSpecificationExecutor<ChipDetail>, PagingAndSortingRepository<ChipDetail, Long> {

}
