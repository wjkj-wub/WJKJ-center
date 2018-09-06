package com.miqtech.master.dao;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.utils.SqlJoiner;

/**
 * 主要用于原生sql查询
 */
@Component
public class QueryDao {

	@Autowired
	private EntityManagerFactory entityManagerFactory;
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryDao.class);

	/**
	 * 查询的结果是实体
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> queryObject(String sql, Class<T> classType) {
		EntityManager em = entityManagerFactory.createEntityManager();
		Query query = em.createNativeQuery(sql, classType); //执行查询，返回的是实体列表
		List resultList = null;
		try {
			resultList = query.getResultList();
		} catch (Exception e) {
			LOGGER.error("sql[{}]查询异常:{}", sql, e.getMessage());
		} finally {
			em.close();
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public <T> T query(String sql) {
		EntityManager em = entityManagerFactory.createEntityManager();
		Query query = em.createNativeQuery(sql);
		T singleResult = null;
		try {
			singleResult = (T) query.getSingleResult();
		} catch (Exception e) {
			if (!(e instanceof NoResultException)) {
				LOGGER.error("sql[{}]查询异常:{}", sql, e.getMessage());
			}
		} finally {
			em.close();
		}
		return singleResult;
	}

	/**
	 * 更新和插入操作
	 */
	public int update(String sql) {
		EntityManager em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query query = em.createNativeQuery(sql);
		int executeUpdate = 0;
		try {
			executeUpdate = query.executeUpdate();
			em.getTransaction().commit();
		} catch (Exception e) {
			LOGGER.error("sql[{}]查询异常:{}", sql, e.getMessage());
		} finally {
			em.close();
		}
		return executeUpdate;
	}

	/**
	 * 更新和插入操作 有参
	 */
	public int update(String sql, Map<String, Object> params) {
		EntityManager em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query query = em.createNativeQuery(sql);
		fillParams(query, params);
		int executeUpdate = 0;
		try {
			executeUpdate = query.executeUpdate();
			em.getTransaction().commit();
		} catch (Exception e) {
			LOGGER.error("sql[{}]查询异常:{}", sql, e.getMessage());
		} finally {
			em.close();
		}
		return executeUpdate;
	}

	/**
	 * 查询无参sql对应的单一Map结果集
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> querySingleMap(String sql) {
		EntityManager em = entityManagerFactory.createEntityManager();
		Query query = em.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		List<Map<String, Object>> resultList = null;
		try {
			resultList = query.getResultList();
		} catch (Exception e) {
			LOGGER.error("sql[{}]查询异常:{}", sql, e.getMessage());
		} finally {
			em.close();
		}
		if (CollectionUtils.isNotEmpty(resultList)) {
			return resultList.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 查询有参数sql对应的单一Map结果集
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> querySingleMap(String sql, Map<String, Object> params) {
		EntityManager em = entityManagerFactory.createEntityManager();
		Query query = em.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		fillParams(query, params);
		List<Map<String, Object>> resultList = null;
		try {
			resultList = query.getResultList();
		} catch (Exception e) {
			LOGGER.error("sql[{}]查询异常:{}", sql, e.getMessage());
		} finally {
			em.close();
		}
		if (CollectionUtils.isNotEmpty(resultList)) {
			return resultList.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 查询sql对应的Map结果集
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map<String, Object>> queryMap(String sql) {
		EntityManager em = entityManagerFactory.createEntityManager();
		Query query = em.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		List resultList = null;
		try {
			resultList = query.getResultList();
		} catch (Exception e) {
			LOGGER.error("sql[{}]查询异常:{}", sql, e.getMessage());
		} finally {
			em.close();
		}
		return resultList;
	}

	/**
	 * 查询sql对应的Map结果集
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map<String, Object>> queryMap(String sql, Map<String, Object> params) {
		EntityManager em = entityManagerFactory.createEntityManager();
		Query query = em.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		fillParams(query, params);
		List resultList = null;
		try {
			resultList = query.getResultList();
		} catch (Exception e) {
			LOGGER.error("sql[{}]查询异常:{}", sql, e.getMessage());
		} finally {
			em.close();
		}
		return resultList;
	}

	/**
	 * 查询有参数sql返回对象list
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> queryObjectList(String sql, Map<String, Object> params, Class<T> classType) {
		EntityManager em = entityManagerFactory.createEntityManager();
		Query query = em.createNativeQuery(sql, classType); //执行查询，返回的是实体列表
		fillParams(query, params);
		List<T> resultList = null;
		try {
			resultList = query.getResultList();
		} catch (Exception e) {
			LOGGER.error("sql[{}]查询异常:{}", sql, e.getMessage());
		} finally {
			em.close();
		}
		return resultList;
	}

	/**
	 * 查询有参数sql返回对象T
	 */
	@SuppressWarnings("unchecked")
	public <T> T query(String sql, Map<String, Object> params, Class<T> classType) {
		EntityManager em = entityManagerFactory.createEntityManager();
		Query query = em.createNativeQuery(sql, classType); //执行查询，返回的是实体列表
		fillParams(query, params);
		T singleResult = null;
		try {
			singleResult = (T) query.getSingleResult();
		} catch (Exception e) {
			LOGGER.error("sql[{}]查询异常:{}", sql, e.getMessage());
		} finally {
			em.close();
		}
		return singleResult;
	}

	private void fillParams(Query query, Map<String, Object> params) {
		if (MapUtils.isNotEmpty(params)) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * 产生in查询字符并设置参数
	 * @param <T>
	 */
	public <T> String getInQueryStr(String paramName, List<T> paramsValues, Map<String, Object> params) {
		String datesStr = "";
		for (int i = 0; i < paramsValues.size(); i++) {
			if (StringUtils.isNotBlank(datesStr)) {
				datesStr = SqlJoiner.join(datesStr, ", ");
			}

			datesStr = SqlJoiner.join(datesStr, ":", paramName, String.valueOf(i));
			params.put(paramName + i, paramsValues.get(i));
		}
		return datesStr;
	}

}
