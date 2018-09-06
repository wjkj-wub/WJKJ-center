package com.miqtech.master.admin.web.task;

import com.miqtech.master.consts.taskMatch.TaskMatchConstant;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchEnter;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchRank;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchTheme;
import com.miqtech.master.service.pc.taskmatch.TaskMatchEnterService;
import com.miqtech.master.service.pc.taskmatch.TaskMatchRankService;
import com.miqtech.master.service.pc.taskmatch.TaskMatchThemeService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author shilina
 */
@Component
public class TaskMatchTask {
	@Autowired
	private TaskMatchThemeService taskMatchThemeService;
	@Autowired
	private TaskMatchRankService taskMatchRankService;
	@Autowired
	private TaskMatchEnterService taskMatchEnterService;

	/**
	 * 主题任务赛-修改主题任务状态定时任务
	 */
	@Scheduled(cron = "0 0 0/1 * * ?")
	public void changeStatusTask() {
		List<TaskMatchTheme> list = taskMatchThemeService.findByStatus();
		Date now = new Date();
		if (CollectionUtils.isNotEmpty(list)) {
			for (TaskMatchTheme theme : list) {
				if (!now.before(theme.getEndDate())) {
					theme.setStatus(TaskMatchConstant.THEME_STATUS_FINISH);
					theme.setIsRelease(TaskMatchConstant.THEME_UNRELEASED);
					theme.setUpdateDate(now);

					//修改主题任务赛领取表状态信息
					Long id = theme.getId();
					List<TaskMatchEnter> enterList = taskMatchEnterService.findByTaskIdAndTypeAndStatus(id,
							TaskMatchConstant.TASK_TYPE_THEME, TaskMatchConstant.THEME_STATUS_PROCESS);
					if (CollectionUtils.isNotEmpty(enterList)) {
						for (TaskMatchEnter enter : enterList) {
							enter.setStatus(TaskMatchConstant.THEME_STATUS_FINISH);
						}
						taskMatchEnterService.save(enterList);

					}
				} else if (!now.before(theme.getBeginDate())) {
					if (TaskMatchConstant.THEME_RELEASED.equals(theme.getIsRelease())) {
						theme.setStatus(TaskMatchConstant.THEME_STATUS_PROCESS);
						theme.setUpdateDate(now);
					}

				}
			}
			taskMatchThemeService.save(list);
		}
	}

	/**
	 * 排行榜任务赛-修改排行榜任务状态定时任务
	 */
	@Scheduled(cron = "0 0/1 * * * ?")
	public void changeStatusMatchRankTask() {
		List<TaskMatchRank> list = taskMatchRankService.findByStatus();
		Date now = new Date();
		if (CollectionUtils.isNotEmpty(list)) {
			for (TaskMatchRank rank : list) {
				if (rank.getEndDate() != null && now.after(rank.getEndDate())) {
					rank.setStatus(TaskMatchConstant.RANK_STATUS_INSETTLE);
				} else if (rank.getStartDate() != null && now.after(rank.getStartDate()) && rank.getEndDate() != null
						&& now.before(rank.getEndDate())
						&& !TaskMatchConstant.RANK_STATUS_PROCESS.equals(rank.getStatus())) {
					rank.setStatus(TaskMatchConstant.RANK_STATUS_PROCESS);
				} else if (rank.getEnterDate() != null && now.after(rank.getEnterDate()) && rank.getStartDate() != null
						&& now.before(rank.getStartDate())
						&& !TaskMatchConstant.RANK_STATUS_ENROLLMENT.equals(rank.getStatus())) {
					rank.setStatus(TaskMatchConstant.RANK_STATUS_ENROLLMENT);
				}
			}
			taskMatchRankService.save(list);
		}
	}
}
