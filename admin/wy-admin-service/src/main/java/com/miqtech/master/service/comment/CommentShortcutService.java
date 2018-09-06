package com.miqtech.master.service.comment;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.comment.CommentShortcutDao;
import com.miqtech.master.entity.comment.CommentShortcut;

/**
 * 娱乐赛评论service
 */
@Component
public class CommentShortcutService {
	@Autowired
	private CommentShortcutDao amuseShortcutCommentDao;
	@Autowired
	private QueryDao queryDao;

	/**
	 * 保存
	 * @return
	 */
	public void save(CommentShortcut amuseShortcutComment) {
		if (amuseShortcutCommentDao != null) {
			amuseShortcutCommentDao.save(amuseShortcutComment);
		}
	}

	/**
	 * 删除
	 * @return
	 */
	public void delete(Integer id) {
		String sql = "update comment_shortcut set is_valid=0 where id=" + id;
		queryDao.update(sql);
	}

	/**
	 * 根据ID查实体
	 */
	public CommentShortcut findById(Long id) {
		return amuseShortcutCommentDao.findByIdAndValid(id, 1);
	}

	public List<CommentShortcut> findListByValid() {
		return amuseShortcutCommentDao.findByValidOrderBySortNum(1);
	}

	public Map<String, Object> findBeforeBySortNum(int sortnum) {
		String sql = "select id,sort_num sortNum from comment_shortcut where  is_valid=1 and  sort_num<" + sortnum
				+ " order by sort_num desc limit 1";
		return queryDao.querySingleMap(sql);
	}

	public Map<String, Object> findAfterBySortNum(int sortnum) {
		String sql = "select id,sort_num sortNum from comment_shortcut where is_valid=1 and sort_num>" + sortnum
				+ " order by sort_num limit 1";
		return queryDao.querySingleMap(sql);
	}

	public void updateSortNumById(Map<String, Object> commentnext) {
		String sql = "update comment_shortcut set sort_num=" + commentnext.get("sortNum") + " where id="
				+ commentnext.get("id");
		queryDao.update(sql);
	}

	public int getMaxSortNum() {
		String sql = "select max(sort_num) from comment_shortcut where is_valid=1 ";
		Number sortNum = queryDao.query(sql);
		if (sortNum == null) {
			return 0;
		}
		return sortNum.intValue();
	}

	public int countValid() {
		String sql = "select count(1) from comment_shortcut where is_valid=1 ";
		Number count = queryDao.query(sql);
		if (count == null) {
			return 0;
		}
		return count.intValue();
	}

}
