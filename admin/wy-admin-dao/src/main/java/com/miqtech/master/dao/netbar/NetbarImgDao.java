package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarImg;

/**
 * 网吧图片操作DAO
 */
public interface NetbarImgDao extends PagingAndSortingRepository<NetbarImg, Long>, JpaSpecificationExecutor<NetbarImg> {

	/**
	 * 根据临时网吧信息id查找网吧的环境图片
	 * @param id 临时网吧id
	 */
	List<NetbarImg> findByTmpNetbarId(Long id);

	/**
	 * 根据网吧信息id查找网吧的环境图片
	 * @param id 网吧id
	 */
	List<NetbarImg> findByNetbarId(Long id);

	/**
	 * 查询网吧的有效环境图
	 * @param netbarId 网吧id
	 * @param valid 图片状态 1未删除 0已删除
	 * @param verified 图片审核状态 0未审核通过 1审核通过
	 * @return 所有有效的网吧环境图
	 */
	List<NetbarImg> findByNetbarIdAndValidAndVerified(long netbarId, int valid, int verified);

	NetbarImg findByUrl(String url);

	List<NetbarImg> findByTmpNetbarIdAndVerified(Long id, int verified);

}