package com.miqtech.master.service.application;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.common.ApplicationVersionDao;
import com.miqtech.master.entity.common.ApplicationVersion;

/**
 * 客户端版本
 */
@Component
public class ApplicationVersionService {

	@Autowired
	private ApplicationVersionDao applicationVersionDao;

	/**
	 * 根据类别查找版本信息
	 * @param type 1-安卓;2-IOS
	 */
	public ApplicationVersion findByType(int type, int systemType) {
		return applicationVersionDao.findByTypeAndValidAndSystemType(type, 1, systemType);
	}

	/**
	 * 查找所有的客户端版本信息
	 */
	public List<ApplicationVersion> findAll() {
		return applicationVersionDao.findAll(new Specification<ApplicationVersion>() {
			@Override
			public Predicate toPredicate(Root<ApplicationVersion> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return null;
			}
		});
	}

	/**
	 * 根据id查找版本信息
	 */
	public ApplicationVersion findById(long id) {
		return applicationVersionDao.findOne(id);
	}

	/**
	 * 保存客户端版本信息
	 */
	public ApplicationVersion save(ApplicationVersion appVersionInDB) {
		return applicationVersionDao.save(appVersionInDB);
	}

	/**
	 * 删除版本信息
	 */
	public void delete(long id) {
		applicationVersionDao.delete(id);
	}
}