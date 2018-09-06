package com.miqtech.master.consts.pc;

/**
 * 扩展信息，及获取娱币和积分的类型
 *
 * @author zhangyuqi
 * @create 2017年07月25日
 */
public class UserExtendConstant {

	/**
	 * 私有化，防止创建实例
	 */
	private UserExtendConstant() {
	}

	/**================ 积分类型======================== */
	public static final Byte COIN_TYPE_CONFRONT = 26;// 对抗赛类型
	public static final Byte COIN_TYPE_RECHARGE = 27;// 积分充值
	public static final Byte COIN_TYPE_TASK_MATCH = 28;//

	/**================ 娱币类型======================== */
	public static final Byte CHIP_TYPE_CONFRONT = 15;// 对抗赛类型
	public static final Byte CHIP_TYPE_RECHARGE = 16;// 积分充值
	public static final Byte CHIP_TYPE_TASK_MATCH = 17;// 任务赛类型
}
