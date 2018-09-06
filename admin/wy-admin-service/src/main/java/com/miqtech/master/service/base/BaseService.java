package com.miqtech.master.service.base;

import java.util.List;
import java.util.Map;

import com.miqtech.master.vo.PageVO;

public class BaseService {

	public void setVO(Number total, List<Map<String, Object>> list, PageVO vo, Integer page, Integer pageSize) {
		if (total != null) {
			vo.setTotal(total.intValue());
			if (page * pageSize >= total.intValue()) {
				vo.setIsLast(1);
			}
		}
		vo.setList(list);
	}

}
