package com.miqtech.master.consts;

/**
 * 操作日志相关常量
 */
public class OperateLogConstant {

	private OperateLogConstant() {
		super();
	}

	public static final Integer SYS_TYPE_ADMIN = 1;// 管理后台
	public static final Integer SYS_TYPE_MALL = 2;// 商城后台

	public static final Integer TYPE_ADMIN_VERIFY_PASSED = 1;// 审核通过
	public static final Integer TYPE_ADMIN_VERIFY_REFUSE = 2;// 审核拒绝
	public static final Integer TYPE_ADMIN_APPEAL_PASSED = 3;// 申诉通过
	public static final Integer TYPE_ADMIN_APPEAL_REFUSE = 4;// 申诉拒绝
	public static final Integer TYPE_ADMIN_APPEAL_DEAL = 5;// 申诉处理
	public static final Integer TYPE_ADMIN_GRANT = 6;// 发放
	public static final Integer TYPE_ADMIN_INVENTORY_COMMODITY_DELETE = 7;// 删除库存商品
	public static final Integer TYPE_ADMIN_RESOURCE_ADD_COMMODITY = 8;// 录入商品
	public static final Integer TYPE_ADMIN_RESOURCE_RELEASE_COMMODITY = 9;// 发布商品
	public static final Integer TYPE_ADMIN_RESOURCE_CLOSE_COMMODITY = 10;// 下架商品
	public static final Integer TYPE_ADMIN_RESOURCE_ADD_ORDER = 11;// 添加订单
	public static final Integer TYPE_ADMIN_RESOURCE_CANCEL_ORDER = 12;// 取消订单

}
