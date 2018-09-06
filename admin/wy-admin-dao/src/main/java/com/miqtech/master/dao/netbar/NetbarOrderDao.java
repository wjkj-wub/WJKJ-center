package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.miqtech.master.entity.netbar.NetbarOrder;

/**
 * 网吧订单操作DAO
 */
public interface NetbarOrderDao
		extends PagingAndSortingRepository<NetbarOrder, Long>, JpaSpecificationExecutor<NetbarOrder> {

	/**
	 * 根据用户id查找订单信息(按创建时间降序)
	 * @param userId 用户id
	 */
	List<NetbarOrder> findByUserIdOrderByCreateDateDesc(Long userId);

	/**
	 * 根据预定订单id查找支付信息
	 * @param reserveId 预定订单id
	 */
	NetbarOrder findByReserveId(Long reserveId);

	/**
	 * 根据第三方支付订单id查找支付订单信息
	 * @param outTradeNo 第三方支付id
	 */
	NetbarOrder findByOutTradeNo(String outTradeNo);

	/**
	 * 根据支付订单id 订单状态 数据状态查找支付信息
	 * @param id
	 * @param status -1支付失败 0网民->网娱支付未完成 1网民->网娱完成支付, 网娱->网吧未付款 2网娱->网吧申请付款 3网娱->网吧有异议 4网娱->网吧已结款
	 * @param valid 0-无效；1-有效；2-已支付；
	 */
	NetbarOrder findByIdAndStatusGreaterThanAndValidGreaterThan(Long id, Integer status, Integer valid);

	/**
	 * 查询网吧下的所有可申请支付订单
	 */
	@Query("select t from NetbarOrder t where netbarId = :netbarId and status=1 and merchantComment is not null and totalAmount > 0")
	List<NetbarOrder> findReadypayOrdersByNetbarId(@Param("netbarId") Long netbarId);

	NetbarOrder findByIdAndUserIdAndStatus(Long id, Long userId, int status);
}