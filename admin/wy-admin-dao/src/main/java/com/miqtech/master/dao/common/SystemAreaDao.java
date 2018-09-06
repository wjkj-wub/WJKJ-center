package com.miqtech.master.dao.common;

import com.miqtech.master.entity.common.SystemArea;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 地区操作DAO
 */
public interface SystemAreaDao extends PagingAndSortingRepository<SystemArea, Long>,
		JpaSpecificationExecutor<SystemArea> {

	SystemArea findById(Long id);

	SystemArea findByAreaCode(String code);

	SystemArea findByName(String name);

	List<SystemArea> findByValid(Integer valid);

	List<SystemArea> findByPid(Long pid);

	/**
	 * 查找有效的省级地区
	 */
	@Query("select sa from SystemArea sa where pid=1 and valid=1 order by id")
	List<SystemArea> findValidRoot();

	/**
	 * 查找所有的省级地区
	 */
	@Query("select sa from SystemArea sa where pid=1 order by id")
	List<SystemArea> findAllRoot();

	/**
	 * 查找所有二级城市
	 */
	@Query("select sa from SystemArea sa where pid in (select id from SystemArea where pid=1 ) ")
	List<SystemArea> queryAllCity();

	/**
	 * 查找某个地区的所有有效子地区
	 */
	@Query("select sa from SystemArea sa where areaCode like :likeCode and areaCode != :code and valid=1 order by id")
	List<SystemArea> findValidChildren(@Param("likeCode") String likeCode, @Param("code") String code);

	/**
	 * 查找某个地区的所有子地区
	 */
	@Query("select sa from SystemArea sa where areaCode like :likeCode and areaCode != :code order by id")
	List<SystemArea> findAllChildren(@Param("likeCode") String likeCode, @Param("code") String code);

	/**
	 * 通过areaCode、areaCodes查询
	 */
	List<SystemArea> findByAreaCodeLikeAndAreaCodeNotLikeAndAreaCodeIn(String likeAreaCode, String notLikeAreaCode,
			String[] areaCodes);

	/**
	 * 通过areaCode、areaCodes查询
	 */
	List<SystemArea> findByValidAndAreaCodeLikeAndAreaCodeNotLikeAndAreaCodeIn(int valid, String likeAreaCode,
			String notLikeAreaCode, String[] areaCodes);

	/**
	 * 通过areaCode查询
	 */
	List<SystemArea> findByValidAndAreaCodeNotLike(int valid, String notLikeAreaCode);

	/**
	 * 通过areaCode查询
	 */
	List<SystemArea> findByAreaCodeNotLike(String notLikeAreaCode);

	/**
	 * 查找所有有效的二级城市
	 */
	@Query("select sa from SystemArea sa where pid in (select id from SystemArea where pid=1 and valid=1) and valid=1")
	List<SystemArea> queryAllValidCity();

	/**
	 * 根据城市名称查找已开通的地区
	 * @param name
	 */
	@Query("select sa from SystemArea sa where name like :name and valid=1 order by id")
	List<SystemArea> findValidByName(@Param("name") String name);

	/**
	 * 查找某个用户(录入人员或审核人员)有效的省级地区
	 */
	@Query("select sa from SystemArea sa where pid=1 and valid=1 and id in(select areaId from SysUserArea where sysUserId=:userId) order by id")
	List<SystemArea> findValidRootLimit(@Param("userId") Long userId);

	/**
	 * 查找某个用户(录入人员或审核人员)某个地区下的有效的地区信息
	 */
	@Query("select sa from SystemArea sa where pid=(select id from SystemArea where areaCode=:areaCode) and id in(select areaId from SysUserArea where sysUserId=:userId) order by id")
	List<SystemArea> findValidChildrenLimit(@Param("areaCode") String areaCode, @Param("userId") Long userId);

	/**
	 * 查找所有
	 */
	@Query("select sa from SystemArea sa where area_code is not null")
	@Override
	List<SystemArea> findAll();

}