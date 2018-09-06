package com.miqtech.master.utils;

public class PageUtils {

	private PageUtils() {
		super();
	}

	public static final int ADMIN_DEFAULT_PAGE_SIZE = 30;//后台管理系统分页每页数据

	public static int isBottom(int page, long count) {
		return count > page * ADMIN_DEFAULT_PAGE_SIZE ? 0 : 1;
	}

	public static int calcTotalPage(long count) {
		return count == 0 ? 1 : (int) Math.ceil((double) count / (double) ADMIN_DEFAULT_PAGE_SIZE);
	}

	public static String getLimitSql(int page) {
		String limit = StringUtils.EMPTY;
		Integer pageSize = ADMIN_DEFAULT_PAGE_SIZE;
		page = getPage(page);
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}
		return limit;
	}

	public static String getLimitSql(int page, Integer pageSize) {
		page = getPage(page);
		if (pageSize == null || pageSize <= 0) {
			return getLimitSql(page);
		}
		String limit = StringUtils.EMPTY;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}
		return limit;
	}

	/**
	 * 根据当前页获取 limit 限制语句
	 * page 为空或小于1 则默认为 1
	 * @param page
	 * @return
	 */
	public static int getPage(Integer page) {
		return page == null || page <= 0 ? 1 : page;
	}

	public static int calcStart(int page) {
		page = page > 0 ? page : 1;
		return (page - 1) * ADMIN_DEFAULT_PAGE_SIZE;
	}

	//----------------------------------API ----------------------------------
	public static final int API_DEFAULT_PAGE_SIZE = 10;//api接口分页默认每页数据

	/**
	 * api接口分页数据:当前第几页数据
	 * @param page
	 * @return 最终查询页数
	 */
	public static int apiPage(Integer page) {
		return null == page || page == 0 ? 1 : page;
	}

	/**
	 * api接口分页数据:每页数据条数
	 * @param pageSize
	 * @return 每页多少条数据
	 */
	public static int apiPageSize(Integer pageSize) {
		return null == pageSize || pageSize <= 0 ? API_DEFAULT_PAGE_SIZE : pageSize;
	}

	public static int apiIsBottom(int page, long count) {
		return count <= page * API_DEFAULT_PAGE_SIZE ? 1 : 0;
	}

}
