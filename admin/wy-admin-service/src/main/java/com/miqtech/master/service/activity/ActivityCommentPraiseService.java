package com.miqtech.master.service.activity;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.Msg4UserType;
import com.miqtech.master.consts.MsgConstant;
import com.miqtech.master.dao.activity.ActivityCommentPraiseDao;
import com.miqtech.master.dao.amuse.AmuseActivityCommentDao;
import com.miqtech.master.dao.user.UserInfoDao;
import com.miqtech.master.entity.activity.ActivityCommentPraise;
import com.miqtech.master.entity.amuse.AmuseActivityComment;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.thirdparty.service.JpushService;

@Component
public class ActivityCommentPraiseService {
	@Autowired
	private AmuseActivityCommentDao amuseActivityCommentDao;
	@Autowired
	private ActivityCommentPraiseDao activityCommentPraiseDao;
	@Autowired
	private JpushService msgOperateService;
	@Autowired
	private UserInfoDao userInfoDao;

	public int doPraise(Long userId, Long commentId) {
		AmuseActivityComment comment = amuseActivityCommentDao.findOne(commentId);
		if (comment != null) {
			if (comment.getValid() == 1) {
				ActivityCommentPraise praise = activityCommentPraiseDao.findByUserIdAndCommentId(userId, commentId);
				if (praise == null) {
					praise = new ActivityCommentPraise();
					praise.setUserId(userId);
					praise.setCommentId(commentId);
					praise.setValid(1);
					praise.setCreateDate(new Date());
					if (!comment.getUserId().equals(userId)) {
						UserInfo user = userInfoDao.findOne(userId);
						StringBuilder msg = new StringBuilder(user.getNickname());
						msg.append("赞了你");
						msgOperateService.notifyMemberAliasMsg(Msg4UserType.COMMENT_PRAISE.ordinal(),
								comment.getUserId(), MsgConstant.PUSH_MSG_TYPE_COMMENT_PRAISE, "评论点赞消息",
								msg.toString(), true,
								comment.getParentId() == 0 ? comment.getId() : comment.getParentId());
					}
				} else {
					praise.setValid(praise.getValid() == 1 ? 0 : 1);
				}
				activityCommentPraiseDao.save(praise);
				return 0;
			} else {
				return 1;//评论已删除
			}
		} else {
			return 1;//评论不存在
		}
	}
}
