package com.miqtech.master.service.netbar;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarImgDao;
import com.miqtech.master.dao.netbar.NetbarInfoDao;
import com.miqtech.master.dao.netbar.NetbarInfoTmpDao;
import com.miqtech.master.dao.netbar.NetbarMerchantDao;
import com.miqtech.master.entity.netbar.NetbarImg;
import com.miqtech.master.entity.netbar.NetbarInfo;
import com.miqtech.master.entity.netbar.NetbarInfoTmp;
import com.miqtech.master.entity.netbar.NetbarMerchant;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 网吧临时表service
 */
@Component
public class NetbarInfoTmpService {
	@Autowired
	private NetbarInfoTmpDao netbarInfoTmpDao;
	@Autowired
	private NetbarInfoDao netbarInfoDao;
	@Autowired
	private NetbarMerchantDao netbarMerchantDao;
	@Autowired
	private NetbarImgDao netbarImgDao;
	@Autowired
	private QueryDao queryDao;

	private static final Logger logger = LoggerFactory.getLogger(NetbarInfoTmpService.class);

	public NetbarInfoTmp save(NetbarInfoTmp netbarInfoTmp) {
		if (netbarInfoTmp.getUpdateDate() == null) {
			netbarInfoTmp.setUpdateDate(netbarInfoTmp.getCreateDate());
		}
		return netbarInfoTmpDao.save(netbarInfoTmp);
	}

	public List<NetbarInfoTmp> findByName(String name) {
		return netbarInfoTmpDao.findByName(name);
	}

	public NetbarInfoTmp findById(Long id) {
		return netbarInfoTmpDao.findOne(id);
	}

	public NetbarInfoTmp findByNetbarId(Long id) {
		return netbarInfoTmpDao.findByNetbarId(id);
	}

	private PageRequest buildPageRequest(int pageNumber) {
		return new PageRequest(pageNumber - 1, PageUtils.ADMIN_DEFAULT_PAGE_SIZE, new Sort(Direction.DESC, "id"));
	}

	public Page<NetbarInfoTmp> page(int page, Map<String, Object> params) {
		PageRequest pageRequest = buildPageRequest(page);
		Specification<NetbarInfoTmp> spec = buildSpecification(params);
		return netbarInfoTmpDao.findAll(spec, pageRequest);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Specification<NetbarInfoTmp> buildSpecification(final Map<String, Object> searchParams) {
		Specification<NetbarInfoTmp> spec = new Specification<NetbarInfoTmp>() {
			@Override
			public Predicate toPredicate(Root<NetbarInfoTmp> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				int status = NumberUtils.toInt(searchParams.get("status").toString());
				List<Predicate> ps = Lists.newArrayList();

				if (searchParams.containsKey("netbarName")) {
					Path namePath = root.get("name");
					Predicate namePredicate = cb.like(namePath, searchParams.get("netbarName").toString());
					ps.add(namePredicate);
				}
				if (searchParams.containsKey("telephone")) {
					Path namePath = root.get("telephone");
					Predicate namePredicate = cb.like(namePath, searchParams.get("telephone").toString());
					ps.add(namePredicate);
				}
				if (searchParams.containsKey("areaCode")) {
					Path areaPath = root.get("areaCode");
					Predicate areaPredicate = cb.like(areaPath, searchParams.get("areaCode").toString());
					ps.add(areaPredicate);
				}
				if (searchParams.containsKey("areaCode")) {
					Path areaPath = root.get("areaCode");
					Predicate areaPredicate = cb.like(areaPath, searchParams.get("areaCode").toString());
					ps.add(areaPredicate);
				}
				if (searchParams.containsKey("dateMin")) {
					Path dateMinPath = root.get("createDate");
					Predicate dateMinPredicate = cb.greaterThanOrEqualTo(dateMinPath,
							(Date) searchParams.get("dateMin"));
					ps.add(dateMinPredicate);
				}
				if (searchParams.containsKey("isApply")) {
					Path isApplyPath = root.get("isApply");
					Predicate isApplyPredicate = cb.equal(isApplyPath,
							NumberUtils.toInt(searchParams.get("isApply").toString()));
					ps.add(isApplyPredicate);
				}

				Path statusPath = root.get("status");
				Predicate statusPredicate = cb.equal(statusPath, status);
				ps.add(statusPredicate);

				Path validPath = root.get("valid");
				Predicate validPredicate = cb.equal(validPath, 1);//有效
				ps.add(validPredicate);

				if (searchParams.containsKey("clerkUserId")) {
					int clerkUserId = NumberUtils.toInt(searchParams.get("clerkUserId").toString());
					if (clerkUserId > 0) {
						Path clerkUserIdPath = root.get("clerkUserId");
						Predicate clerkUserIdPredicate = cb.equal(clerkUserIdPath, clerkUserId);
						ps.add(clerkUserIdPredicate);
					}
				}

				query.where(cb.and(ps.toArray(new Predicate[ps.size()])));
				return query.getRestriction();
			}
		};
		return spec;
	}

	/**
	 * 修改后的临时表数据拷贝到正式数据中
	 */
	public void updateAcceptedTmpDataToNetbarInfo(NetbarInfoTmp netbarInfoTmp) {
		NetbarInfo netbarInfo = netbarInfoDao.findOne(netbarInfoTmp.getNetbarId());
		if (netbarInfo == null) {
			return;
		}
		netbarInfo.setName(netbarInfoTmp.getName());
		netbarInfo.setAddress(netbarInfoTmp.getAddress());
		netbarInfo.setLongitude(netbarInfoTmp.getLongitude());
		netbarInfo.setLatitude(netbarInfoTmp.getLatitude());
		netbarInfo.setPresentation(netbarInfoTmp.getPresentation());
		netbarInfo.setTelephone(netbarInfoTmp.getTelephone());
		netbarInfo.setSeating(netbarInfoTmp.getSeating());
		String icon = netbarInfoTmp.getIcon();
		if (StringUtils.isNotBlank(icon)) {
			netbarInfo.setIcon(icon);
		}
		String iconMedia = netbarInfoTmp.getIconMedia();
		if (StringUtils.isNotBlank(iconMedia)) {
			netbarInfo.setIconMedia(iconMedia);
		}
		String iconThumb = netbarInfoTmp.getIconThumb();
		if (StringUtils.isNotBlank(iconThumb)) {
			netbarInfo.setIconThumb(iconThumb);
		}
		String img = netbarInfoTmp.getImg();
		if (StringUtils.isNotBlank(img)) {
			netbarInfo.setImg(img);
		}
		String imgMedia = netbarInfoTmp.getImgMedia();
		if (StringUtils.isNotBlank(imgMedia)) {
			netbarInfo.setImgMedia(imgMedia);
		}
		String imgThumb = netbarInfoTmp.getImgThumb();
		if (StringUtils.isNotBlank(imgThumb)) {
			netbarInfo.setImgThumb(imgThumb);
		}
		netbarInfo.setPrice(0.0);
		netbarInfo.setPricePerHour(netbarInfoTmp.getPricePerHour());
		netbarInfo.setAreaId(netbarInfoTmp.getAreaId());
		netbarInfo.setAreaCode(netbarInfoTmp.getAreaCode());
		netbarInfo.setCpu(netbarInfoTmp.getCpu());
		netbarInfo.setMemory(netbarInfoTmp.getMemory());
		netbarInfo.setGraphics(netbarInfoTmp.getGraphics());
		netbarInfo.setDisplay(netbarInfoTmp.getDisplay());
		netbarInfoDao.save(netbarInfo);
	}

	/**
	 * 审核通过临时数据
	 */
	public void acceptAndAddToNetbarInfo(NetbarInfoTmp netbarInfoTmp) {
		//临时表数据转换为正式数据
		NetbarInfo netbarInfo = new NetbarInfo();
		Long netbarIdInTmp = netbarInfoTmp.getNetbarId();
		if (netbarIdInTmp != null && netbarIdInTmp.longValue() > 0) {
			netbarInfo = netbarInfoDao.findOne(netbarIdInTmp);
		} else {
			netbarInfo.setCreateDate(new Date());

		}
		String name = netbarInfoTmp.getName();
		if (StringUtils.isNotBlank(name)) {
			netbarInfo.setName(name);
		}

		String address = netbarInfoTmp.getAddress();
		if (StringUtils.isNotBlank(address)) {
			netbarInfo.setAddress(address);
		}
		String areaCode = netbarInfoTmp.getAreaCode();
		if (StringUtils.isNotBlank(areaCode)) {
			netbarInfo.setAreaCode(areaCode);
		}
		Long areaId = netbarInfoTmp.getAreaId();
		if (areaId != null && areaId > 0) {
			netbarInfo.setAreaId(areaId);
		}
		String cpu = netbarInfoTmp.getCpu();
		if (StringUtils.isNotBlank(cpu)) {
			netbarInfo.setCpu(cpu);
		}
		String display = netbarInfoTmp.getDisplay();
		if (StringUtils.isNotBlank(display)) {
			netbarInfo.setDisplay(display);
		}
		Double longitude = netbarInfoTmp.getLongitude();
		if (null != longitude) {
			netbarInfo.setLongitude(longitude);
		}
		Double latitude = netbarInfoTmp.getLatitude();
		if (null != latitude) {
			netbarInfo.setLatitude(latitude);
		}
		String presentation = netbarInfoTmp.getPresentation();
		if (StringUtils.isNotBlank(presentation)) {
			netbarInfo.setPresentation(presentation);
		}
		String telephone = netbarInfoTmp.getTelephone();
		if (StringUtils.isNotBlank(telephone)) {
			netbarInfo.setTelephone(telephone);
		}
		Integer seating = netbarInfoTmp.getSeating();
		if (null != seating) {
			netbarInfo.setSeating(seating);
		}
		String icon = netbarInfoTmp.getIcon();
		if (StringUtils.isNotBlank(icon)) {
			netbarInfo.setIcon(icon);
		}
		String iconMedia = netbarInfoTmp.getIconMedia();
		if (StringUtils.isNotBlank(iconMedia)) {
			netbarInfo.setIconMedia(iconMedia);
		}
		String iconThumb = netbarInfoTmp.getIconThumb();
		if (StringUtils.isNotBlank(iconThumb)) {
			netbarInfo.setIconThumb(iconThumb);
		}
		String img = netbarInfoTmp.getImg();
		if (StringUtils.isNotBlank(img)) {
			netbarInfo.setImg(img);
		}
		String imgMedia = netbarInfoTmp.getImgMedia();
		if (StringUtils.isNotBlank(imgMedia)) {
			netbarInfo.setImgMedia(imgMedia);
		}
		String imgThumb = netbarInfoTmp.getImgThumb();
		if (StringUtils.isNotBlank(imgThumb)) {
			netbarInfo.setImgThumb(imgThumb);
		}
		Double price = netbarInfoTmp.getPrice();
		if (null != price) {
			netbarInfo.setPrice(price);
		}
		String pricePerHour = netbarInfoTmp.getPricePerHour();
		if (StringUtils.isNotBlank(pricePerHour)) {
			netbarInfo.setPricePerHour(pricePerHour);
		}
		String memory = netbarInfoTmp.getMemory();
		if (StringUtils.isNotBlank(memory)) {
			netbarInfo.setMemory(memory);
		}
		String graphics = netbarInfoTmp.getGraphics();
		if (StringUtils.isNotBlank(graphics)) {
			netbarInfo.setGraphics(graphics);
		}
		netbarInfo.setIsRelease(1);//默认发布
		netbarInfo.setReleaseDate(new Date());
		netbarInfo.setValid(1);
		netbarInfo = netbarInfoDao.save(netbarInfo);

		//设置图片的网吧id
		List<NetbarImg> imgs = netbarImgDao.findByTmpNetbarId(netbarInfoTmp.getId());
		Long netbarId = netbarInfo.getId();
		if (CollectionUtils.isNotEmpty(imgs)) {
			for (NetbarImg netbarImg : imgs) {
				netbarImg.setNetbarId(netbarId);
			}
			netbarImgDao.save(imgs);
		}
		//更新临时表中的网吧id,临时表与正式数据关联
		netbarInfoTmp.setNetbarId(netbarId);
		netbarInfoTmp = netbarInfoTmpDao.save(netbarInfoTmp);

		Long merchantId = netbarInfoTmp.getMerchantId();
		if (merchantId != null) {
			NetbarMerchant merchant = netbarMerchantDao.findOne(merchantId);
			if (merchant == null) {
				logger.error("查询商户信息为空,商户id为[{}].", merchantId);
			}
			merchant.setNetbarId(netbarId);
			merchant = netbarMerchantDao.save(merchant);
		} else {
			logger.error("审核网吧临时数据异常,临时网吧数据中的商户id为空,临时网吧id为[{}]", netbarIdInTmp);
		}

		//设置邀请码
		netbarInfo.setInvitationCode(netbarId.toString());
		netbarInfoDao.save(netbarInfo);
	}

	/**
	 * 将正式表数据更新到临时表数据
	 * @param netbarInfo 正式网吧数据
	 * @param tmp 临时网吧数据
	 */
	public void updateNetbarInfoDataToTmp(NetbarInfo netbarInfo, NetbarInfoTmp tmp) {
		tmp.setName(netbarInfo.getName());
		tmp.setAddress(netbarInfo.getAddress());
		tmp.setAreaCode(netbarInfo.getAreaCode());
		tmp.setAreaId(netbarInfo.getAreaId());
		tmp.setAddress(netbarInfo.getAddress());
		tmp.setLongitude(netbarInfo.getLongitude());
		tmp.setLatitude(netbarInfo.getLatitude());
		tmp.setPresentation(netbarInfo.getPresentation());
		tmp.setTelephone(netbarInfo.getTelephone());
		tmp.setSeating(netbarInfo.getSeating());
		tmp.setIcon(netbarInfo.getIcon());
		tmp.setIconMedia(netbarInfo.getIconMedia());
		tmp.setIconThumb(netbarInfo.getIconThumb());
		tmp.setImg(netbarInfo.getImg());
		tmp.setImgMedia(netbarInfo.getImgMedia());
		tmp.setImgThumb(netbarInfo.getImgThumb());
		Double price = netbarInfo.getPrice();
		tmp.setPrice(price == null ? 0 : price);
		tmp.setPricePerHour(netbarInfo.getPricePerHour());
		tmp.setCpu(netbarInfo.getCpu());
		tmp.setMemory(netbarInfo.getMemory());
		tmp.setGraphics(netbarInfo.getGraphics());
		tmp.setDisplay(netbarInfo.getDisplay());
		netbarInfoTmpDao.save(tmp);

	}

	/**
	 * 根据商户id查找临时表数据
	 * @param id 商户账号id
	 */
	public NetbarInfoTmp findByMerchantId(Long id) {
		return netbarInfoTmpDao.findByMerchantId(id);
	}

	/**
	 * 审核人员临时网吧列表
	 */
	public PageVO netbarAreaLimit(Integer status, Long userId, String netbarName, String telephone, String clerkName,
			String areaCode, int page, String dateMin, String dateMax, Integer source) {
		String sql = "";
		PageVO vo = new PageVO();
		String netbarNameSql = "";
		String telephoneSql = "";
		String clerkNameSql = "";
		String areaCodeSql = "";
		String tableSql = "";
		String dateMinSql = StringUtils.EMPTY;
		String dateMaxSql = StringUtils.EMPTY;
		String sourceSql = "";
		if (StringUtils.isNotBlank(netbarName)) {
			netbarNameSql = SqlJoiner.join(" and a.name like '%", netbarName, "%'");
		}
		if (StringUtils.isNotBlank(telephone)) {
			telephoneSql = SqlJoiner.join(" and a.telephone like '%", telephone, "%'");
		}
		if (StringUtils.isNotBlank(clerkName)) {
			tableSql = ",sys_t_user c";
			clerkNameSql = SqlJoiner.join(" and a.clerk_user_id=c.id and c.realname like '%", clerkName, "%'");
		}

		if (StringUtils.isNotBlank(areaCode)) {
			if (areaCode.substring(2, 6).equals("0000")) {
				areaCode = areaCode.substring(0, 2) + "%";
			} else if (areaCode.substring(4, 6).equals("00")) {
				areaCode = areaCode.substring(0, 4) + "%";
			}
			areaCodeSql = SqlJoiner.join(" and a.area_code like '", areaCode, "'");
		}

		if (StringUtils.isNotBlank(dateMin)) {
			dateMinSql = SqlJoiner.join(" and a.update_date >= '", dateMin, "'");
		}
		if (StringUtils.isNotBlank(dateMax)) {
			dateMaxSql = SqlJoiner.join(" and a.update_date <= '", dateMax, "'");
		}
		if (source != null) {
			sourceSql = SqlJoiner.join(" and a.source=", String.valueOf(source));
		}
		sql = SqlJoiner.join("select count(1) total from netbar_t_info_tmp a,sys_t_area b", tableSql,
				" where a.is_valid=1 and a.status=", String.valueOf(status), netbarNameSql, telephoneSql, clerkNameSql,
				areaCodeSql, dateMinSql, dateMaxSql, sourceSql,
				" and a.area_code=b.area_code and b.id in(select area_id from sys_r_user_area where sys_user_id=",
				String.valueOf(userId), ")");
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * pageSize;
		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("status", status);
		params.put("userId", userId);
		params.put("start", start);
		params.put("pageSize", pageSize);
		sql = SqlJoiner.join(
				"select a.*,x.realname from (netbar_t_info_tmp a,sys_t_area b) left join sys_t_user x on a.clerk_user_id=x.id ",
				tableSql, " where a.is_valid=1 and a.status=:status", netbarNameSql, telephoneSql, clerkNameSql,
				areaCodeSql, dateMinSql, dateMaxSql, sourceSql,
				" and a.area_code=b.area_code and b.id in(select area_id from sys_r_user_area where sys_user_id=:userId) limit :start,:pageSize");
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**
	 * 根据录入系统审核用户id查询此用户待审核网吧数量
	 */
	public int countUnAuditorNetbarInfo(Long systemUserId) {
		String sql = SqlJoiner.join(
				"select count(1) total from netbar_t_info_tmp a,sys_t_area b  where a.is_valid=1 and a.status=1 and a.area_code=b.area_code and b.id in(select area_id from sys_r_user_area where sys_user_id=",
				String.valueOf(systemUserId), ")");
		Number totalCount = queryDao.query(sql);
		return totalCount == null ? 0 : totalCount.intValue();
	}
}