package com.miqtech.master.service.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.user.UserValueAddedCardDao;
import com.miqtech.master.entity.user.UserValueAddedCard;

/**
 * 用户增值券功能service
 */
@Component
public class UserValueAddedCardService {

	@Autowired
	private UserValueAddedCardDao userValueAddedCardDao;

	public UserValueAddedCard save(UserValueAddedCard userValueAddedCard) {
		return userValueAddedCardDao.save(userValueAddedCard);
	}

	public List<UserValueAddedCard> findByIdIn(List<Long> ids) {
		return userValueAddedCardDao.findByIdIn(ids);
	}

	public void save(List<UserValueAddedCard> valueAddedCards) {
		userValueAddedCardDao.save(valueAddedCards);

	}
}
