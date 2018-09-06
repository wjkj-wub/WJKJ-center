package com.miqtech.master.dao.common;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.SystemRedbag;

/**
 * 系统红包操作DAO
 */
public interface SystemRedbagDao
		extends PagingAndSortingRepository<SystemRedbag, Long>, JpaSpecificationExecutor<SystemRedbag> {

	/**
	 * 根据红包id和状态查找系统红包信息
	 * @param id 系统红包id
	 * @param valid 0无效 1有效
	 */
	SystemRedbag queryByIdAndValid(Long id, Integer valid);

	/**
	 * 根据红包类型查找系统红包信息
	 * @param type 0首次登陆 1注册绑定 2预约支付 3周红包 4分享红包
	 */
	List<SystemRedbag> queryListByTypeAndValidOrderByMoneyAsc(int type, Integer valid);

	/**
	 * 根据红包类型查找系统红包信息
	 * @param type 0首次登陆 1注册绑定 2预约支付 3周红包 4分享红包
	 * @param valid 0有效 1无效
	 * @param beginTime 开始时间(查找下一次周红包)
	 */
	List<SystemRedbag> findByTypeAndValidAndBeginTimeGreaterThanOrderByBeginTime(int type, int valid, Date beginTime);

	/**
	 * 根据红包类型查找系统红包信息
	 * @param type 0首次登陆 1注册绑定 2预约支付 3周红包 4分享红包
	 * @param valid 0有效 1无效
	 * @param beginTime 开始时间(查找当前周红包)
	 * @param endTime 结束时间(查找当前周红包)
	 */
	SystemRedbag findByTypeAndValidAndBeginTimeLessThanAndEndTimeGreaterThan(int type, int valid, Date beginTime,
			Date endTime);

	/**
	 * 根据红包类别和金额查询红包信息
	 * @param type
	 * @param amount
	 * @return
	 */
	List<SystemRedbag> findByTypeAndMoney(int type, int money);

	List<SystemRedbag> findByTypeAndMoneyInAndValid(int type, List<Integer> moneys, int valid);

	SystemRedbag findByTypeAndValid(int type, int valid);
}