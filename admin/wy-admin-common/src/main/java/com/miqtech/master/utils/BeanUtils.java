package com.miqtech.master.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanUtils {

	private BeanUtils() {
		super();
	}

	private static Logger LOGGER = LoggerFactory.getLogger(BeanUtils.class);

	/**
	 * 将新对象中非null的值 覆盖掉旧对象中相应的值
	 */
	public static <T> T updateBean(T oldBean, T newBean) {
		Field[] fields = oldBean.getClass().getDeclaredFields();
		oldBean = setFields(oldBean, newBean, fields);

		Field[] parentFields = oldBean.getClass().getSuperclass().getDeclaredFields();
		oldBean = setFields(oldBean, newBean, parentFields);

		return oldBean;
	}

	/**
	 * 为所有字段赋值
	 */
	private static <T> T setFields(T oldBean, T newBean, Field[] fields) {
		for (Field field : fields) {
			// 调用get方法，为旧对象设置新值
			Method getter = getGetter(newBean, field);
			if (getter != null) {
				try {
					Object fieldValue = getter.invoke(newBean, new Object[0]);
					if (fieldValue != null) {
						Method setter = getSetter(oldBean, field);
						if (setter != null) {
							setter.invoke(oldBean, fieldValue);
						}
					}
				} catch (Exception e) {
					LOGGER.error("更新属性(" + newBean.getClass() + ":" + field.getName() + ")异常：", e);
				}
			}
		}
		return oldBean;
	}

	/**
	 * 获取get方法
	 */
	private static <T> Method getGetter(T bean, Field field) {
		String methodName = prefixFieldName("get", field.getName());
		try {
			return bean.getClass().getMethod(methodName, new Class[0]);
		} catch (Exception expect) {
		}
		return null;
	}

	/**
	 * 获取set方法
	 */
	private static <T> Method getSetter(T bean, Field field) {
		String methodName = prefixFieldName("set", field.getName());
		try {
			return bean.getClass().getMethod(methodName, field.getType());
		} catch (Exception e) {
			LOGGER.error("获取set方法异常：", e);
		}
		return null;
	}

	/**
	 * 为属性添加前缀
	 */
	private static String prefixFieldName(String prefix, String fieldName) {
		StringBuffer result = new StringBuffer();
		result.append(prefix).append(fieldName.substring(0, 1).toUpperCase()).append(fieldName.substring(1));
		return result.toString();
	}

	public static Map<String, Object> beanToMap(Object entity) {
		Map<String, Object> parameter = new HashMap<String, Object>();
		Class<? extends Object> clazz = entity.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			String fieldName = fields[i].getName();
			Object o = null;
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String getMethodName = "get" + firstLetter + fieldName.substring(1);
			try {
				Method getMethod = clazz.getMethod(getMethodName, new Class[] {});
				o = getMethod.invoke(entity, new Object[] {});
			} catch (Exception e) {
			}
			if (o != null) {
				parameter.put(fieldName, o);
			}
		}
		return parameter;
	}

}
