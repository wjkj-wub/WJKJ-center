package com.miqtech.master.consts.taskMatch;

/**
 * 任务赛常量定义
 *
 * @author zhangyuqi
 * @create 2017年09月07日
 */
public class TaskMatchConstant {
	private TaskMatchConstant() {
	}

	public static final int TASK_TYPE_RANK = 1;//排行榜
	public static final int TASK_TYPE_THEME = 2;//主题

	public static final int RECORD_TYPE_APPLY = 1;//报名

	public static final Byte THEME_STATUS_PREPARE = 0;//准备中
	public static final Byte THEME_STATUS_PROCESS = 1;//进行中
	public static final Byte THEME_STATUS_FINISH = 2;//已结束

	public static final int CONDITION_MODULE_TYPE_THEME = 1;

	/* 任务赛类型 */
	public static final Byte TASK_MATCH_TYPE_RANK = 1; // 排行榜
	public static final Byte TASK_MATCH_TYPE_THEME = 2; // 主题

	/* 费用类型 */
	public static final Byte FEE_TYPE_FREE = 0;// 免费
	public static final Byte FEE_TYPE_COIN = 1;// 积分
	public static final Byte FEE_TYPE_CHIP = 2;// 娱币
	public static final Byte FEE_TYPE_RMB = 3;// 娱币

	/* 任务赛费用记录类型 */
	public static final Byte TASK_MATCH_RECORD_TYPE_ENTER = 1; // 报名
	public static final Byte TASK_MATCH_RECORD_TYPE_AWARD = 2; // 奖励
	public static final Byte TASK_MATCH_RECORD_TYPE_RETURN = 3; // 退费
	public static final Byte TASK_MATCH_RECORD_TYPE_THEME_UPDATE = 4; // 更换主题任务

	public static final Byte THEME_UNRELEASED = 0;//未上架
	public static final Byte THEME_RELEASED = 1;//上架

	/* 排位赛任务状态 */
	public static final Byte RANK_STATUS_NOTENABLED = -1;//-1-未启用
	public static final Byte RANK_STATUS_WAIT4REGIST = 0;// 0-等待报名
	public static final Byte RANK_STATUS_ENROLLMENT = 1;// 1-报名中
	public static final Byte RANK_STATUS_PROCESS = 2; // 2-进行中
	public static final Byte RANK_STATUS_INSETTLE = 3; //3-结算中
	public static final Byte RANK_STATUS_FINISH = 4; //4-已结束

	public static final String TASK_MATCH_THEME_CREATE_LIMIT = "task_match_theme_create_limit";

}
