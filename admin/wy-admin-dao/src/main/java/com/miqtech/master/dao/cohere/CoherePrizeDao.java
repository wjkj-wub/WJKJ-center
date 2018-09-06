package com.miqtech.master.dao.cohere;

import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import com.miqtech.master.entity.cohere.CoherePrize;

public interface CoherePrizeDao extends JpaSpecificationExecutor<CoherePrize>, PagingAndSortingRepository<CoherePrize, Long>{
	public List<CoherePrize> findByActivityIdAndValid(Long activityId,Integer isValid);

}
