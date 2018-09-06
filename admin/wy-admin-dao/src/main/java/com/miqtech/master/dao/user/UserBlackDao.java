package com.miqtech.master.dao.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.user.UserBlack;

/**
 * 黑名单Dao
 */
public interface UserBlackDao extends JpaSpecificationExecutor<UserBlack>, PagingAndSortingRepository<UserBlack, Long> {

	List<UserBlack> findByIdInAndValid(List<Long> ids, Integer valid);

	UserBlack findByUserIdAndAccountAndChannel(Number userId, String account, int channel);

	UserBlack findByUserIdAndChannelAndValidAndIsWhite(long userId, int channel, int valid, int white);

	UserBlack findByAccountAndChannelAndValidAndIsWhite(String account, int channel, int valid, int white);

}
