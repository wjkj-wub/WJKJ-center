package com.miqtech.master.service.netbar.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.resource.NetbarCommodityCategoryDao;
import com.miqtech.master.dao.netbar.resource.NetbarResourceAreaQuottaRaitoDao;
import com.miqtech.master.entity.netbar.resource.NetbarCommodityCategory;
import com.miqtech.master.entity.netbar.resource.NetbarResourceAreaQuottaRaito;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.NetbarCommodityCategoryVO;

@Component
public class NetbarResourcePropertyService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarResourceAreaQuottaRaitoDao netbarResourceAreaQuottaRaitoDao;
	@Autowired
	private NetbarCommodityCategoryDao netbarCommodityCategoryDao;

	public List<Map<String, Object>> netbarResourceAreaRaitoList() {
		String sql = "select a.name, a.area_code,b.vip_ratio,b.gold_ratio,b.jewel_ratio, c.vip_ratio next_vip_ratio, c.gold_ratio next_gold_ratio, c.jewel_ratio next_jewel_ratio from sys_t_area a left join ( select area_code,vip_ratio,gold_ratio,jewel_ratio from netbar_resource_area_quotta_raito where is_valid=1 ) b on a.area_code = b.area_code left join ( select area_code, vip_ratio, gold_ratio, jewel_ratio from netbar_resource_area_quotta_raito where is_valid = 2 ) c on a.area_code = c.area_code where a.is_valid = 1 and a.area_code like '%0000' order by a.area_code";
		return queryDao.queryMap(sql);
	}

	public void updateRatio(String areaCode, Float vip_ratio, Float gold_ratio, Float jewel_ratio) {
		String sql = SqlJoiner.join(
				"update netbar_resource_area_quotta_raito set is_valid=0 where is_valid=2 and area_code='", areaCode,
				"'");
		queryDao.update(sql);
		NetbarResourceAreaQuottaRaito areaRaito = new NetbarResourceAreaQuottaRaito();
		areaRaito.setAreaCode(areaCode);
		areaRaito.setVipRatio(vip_ratio);
		areaRaito.setGoldRatio(gold_ratio);
		areaRaito.setJewelRatio(jewel_ratio);
		areaRaito.setCreateDate(new Date());
		areaRaito.setValid(2);
		netbarResourceAreaQuottaRaitoDao.save(areaRaito);
	}

	public List<NetbarCommodityCategory> findByValidAndPid(int valid, long pid) {
		return netbarCommodityCategoryDao.findByValidAndPid(valid, pid);
	}

	public void saveNetbarCommodityCategory(NetbarCommodityCategory netbarCommodityCategory) {
		netbarCommodityCategoryDao.save(netbarCommodityCategory);
	}

	public NetbarCommodityCategory findCategoryById(Long id) {
		return netbarCommodityCategoryDao.findOne(id);
	}

	public List<NetbarCommodityCategoryVO> netbarCommodityCategoryList() {
		List<NetbarCommodityCategory> list = netbarCommodityCategoryDao.findAllCategory();
		List<NetbarCommodityCategoryVO> result = new ArrayList<NetbarCommodityCategoryVO>();
		NetbarCommodityCategoryVO vo = null;
		for (NetbarCommodityCategory obj : list) {
			if (obj.getPid() == 0) {
				if (vo != null) {
					result.add(vo);
					vo = new NetbarCommodityCategoryVO();
					vo.setParent(obj);
				} else {
					vo = new NetbarCommodityCategoryVO();
					vo.setParent(obj);
				}
			} else {
				vo.getSub().add(obj);
			}
		}
		result.add(vo);
		return result;
	}

	public void autoUpdate() {
		String sql = "update netbar_resource_area_quotta_raito set is_valid=0 where is_valid=1 and area_code in(SELECT area_code from (select area_code from  netbar_resource_area_quotta_raito where is_valid=2)a)";
		queryDao.update(sql);
		sql = "update netbar_resource_area_quotta_raito set is_valid=1 where is_valid=2";
		queryDao.update(sql);
	}

	public List<Map<String, Object>> trend(String areaCode) {
		return queryDao.queryMap(SqlJoiner.join(
				"select date_format(create_date, '%Y-%m-%d') create_date,vip_ratio,gold_ratio,jewel_ratio from netbar_resource_area_quotta_raito where is_valid<>2 and area_code='",
				areaCode, "' order by create_date limit 0,7"));
	}

	public Map<String, Object> netbarResourceDetail(String id, String netbarId) {
		String sql = "select d.name netbar_name,a.id,a.interest_num,a.name, date_format(a.settl_date, '%Y-%m-%d') settl_date, c. name type, b.introduce from netbar_resource_commodity_property a, netbar_resource_commodity b, netbar_commodity_category c,netbar_t_info d where a.id ="
				+ id + " and a.commodity_id = b.id and b.category_id = c.id and d.id=" + netbarId;
		return queryDao.querySingleMap(sql);
	}

	public void insterest(String id) {
		String sql = "update netbar_resource_commodity_property set interest_num=interest_num+1 where id=" + id;
		queryDao.update(sql);
	}

	public void del(Long id) {
		netbarCommodityCategoryDao.delete(id);
	}

	/**
	 * 返回所有有效的地区配额比例
	 */
	public List<Map<String, Object>> netbarValidRatioList() {
		return queryDao.queryMap(
				"select area_code,gold_ratio,vip_ratio,jewel_ratio from netbar_resource_area_quotta_raito where is_valid = 1  ");
	}
}
