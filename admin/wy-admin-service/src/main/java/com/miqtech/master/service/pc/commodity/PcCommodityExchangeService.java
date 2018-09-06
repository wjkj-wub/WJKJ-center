package com.miqtech.master.service.pc.commodity;

import com.miqtech.master.consts.pc.RateConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.pc.commodity.PcCommodityExchangeDao;
import com.miqtech.master.entity.pc.commodity.PcCommodityExchange;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author shilina
 */
@Service
public class PcCommodityExchangeService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private PcCommodityExchangeDao pcCommodityExchangeDao;
	@Autowired
	private SystemAreaService systemAreaService;

	public PcCommodityExchange findById(Long id) {
		return pcCommodityExchangeDao.findByIdAndIsValid(id, true);
	}

	public void save(List<PcCommodityExchange> list) {
		pcCommodityExchangeDao.save(list);
	}

	public PageVO list(Integer status, Integer type, String start, String end, String nickname, Integer page,
			Integer pageSize) {
		if (page == null || page <= 0) {
			page = 1;
		}
		if (pageSize == null || pageSize <= 0) {
			pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		}
		StringBuilder sb = new StringBuilder();
		if (status != null && status != 0) {
			sb.append(" and pce.status=").append(status);
		}
		if (type != null && type != 0) {
			sb.append(" and pc.type=").append(type);
		}
		if (StringUtils.isNotBlank(start)) {
			sb.append(" and pc.create_date>='").append(DateUtils.stampToDate(start, DateUtils.YYYY_MM_DD)).append("'");
		}
		if (StringUtils.isNotBlank(end)) {
			sb.append(" and pc.create_date<='").append(DateUtils.stampToDate(end, DateUtils.YYYY_MM_DD)).append("'");
		}
		if (StringUtils.isNotBlank(nickname)) {
			sb.append(" and pui.nickname like '%").append(nickname).append("%'");
		}
		String countSql = "SELECT count(*) FROM pc_commodity_exchange pce "
				+ "LEFT JOIN pc_commodity pc ON pce.commodity_id = pc.id "
				+ "left join pc_user_info  pui on pce.user_id=pui.id where pce.is_valid=1 and pce.status between 1 and 4";
		countSql += sb.toString();
		Number count = queryDao.query(countSql);
		if (count == null || count.intValue() <= 0) {
			return new PageVO();
		}
		int startLimit = (page - 1) * pageSize;
		int rate = RateConstant.RMB_TO_CHIP;
		String querySql = "SELECT pce.id,pce.create_date,pui.nickname,pc.type,pc.`name`,pce.num,(pc.cash * " + rate
				+ " + pc.chip) * pce.num totalSum,pce.telephone,pce.qq,pce.address,pce.area_code,pce.`status`"
				+ " FROM pc_commodity_exchange pce" + " LEFT JOIN pc_commodity pc ON pce.commodity_id = pc.id"
				+ " LEFT JOIN pc_user_info pui ON pce.user_id = pui.id"
				+ " where pce.status between 1 and 4 and pce.is_valid=1" + sb.toString() + " group by pce.id limit "
				+ startLimit + "," + pageSize;
		List<Map<String, Object>> list = queryDao.queryMap(querySql);
		for (Map<String, Object> map : list) {
			Object code = map.get("area_code");
			if (code != null) {
				String areaCode = code.toString();
				if (StringUtils.isNotBlank(areaCode)) {
					Object add = map.get("address");
					StringBuilder address = new StringBuilder();
					if (add != null) {
						address.append(add.toString());
					}
					map.put("address", systemAreaService.getAreaInfoByCode(areaCode) + address.toString());
				}
			}
		}
		return new PageVO(page, list, count, pageSize);
	}
}
