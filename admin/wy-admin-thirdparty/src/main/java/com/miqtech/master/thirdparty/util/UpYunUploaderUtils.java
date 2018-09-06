package com.miqtech.master.thirdparty.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.java.com.UpYun;
import main.java.com.UpYun.PARAMS;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.miqtech.master.utils.HttpRequestUtil;
import com.tencent.common.MD5;

public class UpYunUploaderUtils {
	private static final String HLS_API_URL = "http://p0.api.upyun.com/pretreatment/";// 音视频处理接口
	private static final String HLS_STATUS_API_URL = "http://p0.api.upyun.com/status";// 音视频处理进度查询

	public static final String FORM_API_URL_AUTO = "http://v0.api.upyun.com/";
	public static final String IMG_SERVER_URL = "http://img.wangyuhudong.com/";

	// 运行前先设置好以下三个参数
	public static final String BUCKET_NAME = "wymaster";
	private static final String OPERATOR_NAME = "master";
	private static final String OPERATOR_PWD = "miquwy888";
	private static UpYun upyun = new UpYun(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
	private static final String FORM_API_KEY = "1KY5fMHAv8KST2xf4LUCc2ss9Ec=";

	/** 根目录 */
	private static final String DIR_ROOT = "/";

	/**
	 * 上传文件
	 */
	public static boolean uploadImg(File file, String path, String fileName) {
		String filePath = genFilePath(path, fileName);

		// 设置待上传文件的 Content-MD5 值
		// 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 NotAcceptable 错误
		try {
			upyun.setContentMD5(UpYun.md5(file));
			// 上传文件，并自动创建父级目录（最多10级）
			boolean result = upyun.writeFile(filePath, file, true);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 上传文件
	 */
	public static String uploadImgBinary(byte[] datas, String path, String fileName) {
		String filePath = genFilePath(path, fileName);

		// 设置待上传文件的 Content-MD5 值
		// 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 NotAcceptable 错误
		//upyun.setContentMD5(UpYun.md5(new String(datas)));
		// 上传文件，并自动创建父级目录（最多10级）
		boolean result = upyun.writeFile(filePath, datas, true);
		if (result) {
			return filePath;
		}
		return null;
	}

	/**
	 * 图片做缩略图
	 * <p>
	 * 注意：若使用了缩略图功能，则会丢弃原图
	 * @param size eg:400x400
	 */
	public static boolean thumbImg(File file, String path, String fileName, String size) {
		String filePath = genFilePath(path, fileName);
		// 设置缩略图的参数
		Map<String, String> params = new HashMap<String, String>();
		// 设置缩略图类型，必须搭配缩略图参数值（KEY_VALUE）使用，否则无效
		params.put(PARAMS.KEY_X_GMKERL_TYPE.getValue(), PARAMS.VALUE_FIX_BOTH.getValue());
		// 设置缩略图参数值，必须搭配缩略图类型（KEY_TYPE）使用，否则无效
		params.put(PARAMS.KEY_X_GMKERL_VALUE.getValue(), size);
		// 设置缩略图的质量，默认 95
		params.put(PARAMS.KEY_X_GMKERL_QUALITY.getValue(), "80");
		// 设置缩略图的锐化，默认锐化（true）
		params.put(PARAMS.KEY_X_GMKERL_UNSHARP.getValue(), "true");
		// 若在 upyun 后台配置过缩略图版本号，则可以设置缩略图的版本名称
		// 注意：只有存在缩略图版本名称，才会按照配置参数制作缩略图，否则无效
		params.put(PARAMS.KEY_X_GMKERL_THUMBNAIL.getValue(), "small");
		// 上传文件，并自动创建父级目录（最多10级）
		boolean result;
		try {
			result = upyun.writeFile(filePath, file, true, params);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static String genFilePath(String path, String fileName) {
		String filePath;
		if (StringUtils.isEmpty(path) || StringUtils.equals(DIR_ROOT, path)) {
			filePath = DIR_ROOT + fileName;
		} else {
			// 要传到upyun后的文件路径
			filePath = DIR_ROOT + path + DIR_ROOT + fileName;
		}
		return filePath;
	}

	/**
	 * 分块上传文件
	 */
	public static String hlsFile(String source) {
		if (StringUtils.isBlank(source)) {
			return null;
		}

		Map<String, String> params = Maps.newHashMap();
		params.put("bucket_name", BUCKET_NAME);
		params.put("notify_url", "http://www.wangyuhudong.com/");
		params.put("source", source);
		String tasksStr = "[{\"type\": \"hls\", \"save_as\": \"" + changeExtension(source, "m3u8") + "\"}]";
		String tasks = Base64.encode(tasksStr.getBytes());
		params.put("tasks", tasks);
		params.put("accept", "json");

		Map<String, String> requestProperties = Maps.newHashMap();
		String signature = pretreatmentSignature(params);
		requestProperties.put("Authorization", "UPYUN " + OPERATOR_NAME + ":" + signature);

		String response = HttpRequestUtil.sendPost(HLS_API_URL, requestProperties, params);

		JSONObject jsonResult = null;
		Object parseRes = JSONArray.parse(response);
		if (parseRes instanceof JSONArray) {
			return ((JSONArray) parseRes).getString(0);
		} else {
			jsonResult = (JSONObject) parseRes;
		}

		if (jsonResult != null) {
			String taskId = jsonResult.getString("task_id");
			return taskId;
		}
		return null;
	}

	/**
	 * 查询hls切分进度
	 */
	public static String checkHlsFile(String taskId) {
		if (StringUtils.isNotBlank(taskId)) {
			Map<String, String> params = Maps.newHashMap();
			params.put("bucket_name", BUCKET_NAME);
			params.put("task_ids", taskId);

			Map<String, String> requestProperties = Maps.newHashMap();
			String signature = pretreatmentSignature(params);
			requestProperties.put("Authorization", "UPYUN " + OPERATOR_NAME + ":" + signature);

			String response = HttpRequestUtil.sendGet(HLS_STATUS_API_URL, requestProperties, params);

			if (StringUtils.isNotBlank(response)) {
				String progress = JSONObject.parseObject(response).getJSONObject("tasks").getString(taskId);
				if (StringUtils.isNotBlank(progress)) {
					return progress;
				}
			}
		}

		return null;
	}

	/**
	 * 生成音视频处理的签名
	 */
	private static String pretreatmentSignature(Map<String, String> params) {
		if (MapUtils.isEmpty(params)) {
			return null;
		}

		String sign = StringUtils.EMPTY;
		Joiner joiner = Joiner.on("");
		if (MapUtils.isNotEmpty(params)) {
			Set<String> keySet = params.keySet();
			List<String> keys = new ArrayList<>(keySet);

			// 按字典序排序
			keys.sort(new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});

			for (String key : keys) {
				sign = joiner.join(sign, key, MapUtils.getString(params, key));
			}
		}
		sign = joiner.join(OPERATOR_NAME, sign, MD5.MD5Encode(OPERATOR_PWD));

		return MD5.MD5Encode(sign);
	}

	/**
	 * 生成响应的签名
	 */
	@SuppressWarnings("unused")
	private static String responseSignature(String taskId, String timestamp) {
		String sign = OPERATOR_NAME + OPERATOR_PWD + taskId + timestamp;
		return MD5.MD5Encode(sign);
	}

	/**
	 * 替换文件后缀
	 */
	public static String changeExtension(String url, String extension) {
		if (StringUtils.isBlank(url)) {
			return null;
		}
		url = StringUtils.substring(url, 0, StringUtils.lastIndexOf(url, ".") + 1) + extension;
		return url;
	}

	/**
	 * form api policy算法
	 */
	public static String formPolicy(Map<String, Object> params) {
		String jsonParamsStr = JSONObject.toJSONString(params);
		return Base64.encode(jsonParamsStr.getBytes());
	}

	/**
	 * form api signature算法
	 */
	public static String formSignature(String policy) {
		String sign = policy + "&" + FORM_API_KEY;
		return MD5.MD5Encode(sign);
	}
}
