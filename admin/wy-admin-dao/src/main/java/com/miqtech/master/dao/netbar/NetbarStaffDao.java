package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarStaff;

/**
 * 网吧员工数据操作DAO
 */
public interface NetbarStaffDao
		extends PagingAndSortingRepository<NetbarStaff, Long>, JpaSpecificationExecutor<NetbarStaff> {

	NetbarStaff findById(Long id);

	/**
	 * 根据邀请码和状态查找网吧员工信息
	 * @param invitation 邀请码
	 * @param valid 0无效 1有效
	 */
	List<NetbarStaff> findByInvitationCodeAndValid(String invitation, int valid);

	/**
	 * 根据手机号和密码查找网吧
	 * @param phone 员工手机号
	 * @param password 密码
	 */
	NetbarStaff findByTelephoneAndPassword(String phone, String password);

	/**
	 * 根据用户手机号查找网吧员工信息
	 * @param phone 员工手机号
	 */
	NetbarStaff findByTelephoneAndValid(String phone, int valid);

	NetbarStaff findByTelephoneAndPasswordAndValid(String phone, String pwd, int valid);

	List<NetbarStaff> findByNameOrTelephoneAndValid(String name, String telephone, int valid);

	NetbarStaff findByNameAndValid(String name, int valid);

	List<NetbarStaff> findByTelephoneAndPasswordAndValidOrderByCreateDate(String phone, String pwd, int i);

}