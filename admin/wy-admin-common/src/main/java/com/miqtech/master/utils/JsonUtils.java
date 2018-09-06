package com.miqtech.master.utils;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * api的json序列化工具
 */
public class JsonUtils {

	private JsonUtils() {
		super();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);
	private static ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objectMapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
		objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
	}

	public static String objectToString(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			LOGGER.error("Object->String Error:{}", e);
			return null;
		}
	}

	public static byte[] objectToBytes(Object obj) {
		try {
			return objectMapper.writeValueAsBytes(obj);
		} catch (JsonProcessingException e) {
			LOGGER.error("Object->byte[] Error:{}", e);
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
	}

	public static <T> T stringToObject(String str, Class<T> clazz) {
		try {
			return objectMapper.readValue(str, clazz);
		} catch (IOException e) {
			LOGGER.error("String->Object Error:{}", e);
			return null;
		}
	}

	public static <T> T bytesToObject(byte[] bytes, Class<T> clazz) {
		try {
			return objectMapper.readValue(bytes, clazz);
		} catch (IOException e) {
			LOGGER.error("byte[]->Object Error:{}", e);
			return null;
		}
	}

	public static <T> T inputStreamToObject(InputStream is, Class<T> clazz) {
		try {
			return objectMapper.readValue(is, clazz);
		} catch (IOException e) {
			LOGGER.error("byte[]->Object Error:{}", e);
			return null;
		}
	}

	public static <T> T stringToCollection(String string, Class<?> collectionClass, Class<?>... valueType)
			throws Exception {
		JavaType javaType = objectMapper.getTypeFactory().constructParametrizedType(collectionClass, collectionClass,
				valueType);
		return objectMapper.readValue(string, javaType);
	}

}
