package com.miqtech.master.dao.pc.userExtend;

import com.miqtech.master.entity.pc.userExtends.UserExtend;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 用户扩展信息 DAO
 *
 * @author zhangyuqi
 * @create 2017年09月08日
 */
public interface UserExtendDao
		extends JpaSpecificationExecutor<UserExtend>, PagingAndSortingRepository<UserExtend, Long> {
	UserExtend findByUserIdAndValid(Long userId, Integer valid);
}
