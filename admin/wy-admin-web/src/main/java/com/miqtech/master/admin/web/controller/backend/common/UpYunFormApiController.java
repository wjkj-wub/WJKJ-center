package com.miqtech.master.admin.web.controller.backend.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.thirdparty.util.UpYunUploaderUtils;
import com.miqtech.master.thirdparty.util.img.FileUploadUtil;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.DateUtils;

@Controller
@RequestMapping("upYunFormApi")
public class UpYunFormApiController extends BaseController {

	/**
	 * 通过网页表单上传文件
	 */
	@RequestMapping("upload")
	public ModelAndView formUpload() {
		ModelAndView mv = new ModelAndView("common/upYunFormUpload");
		return mv;
	}

	/**
	 * 表单验证
	 */
	@ResponseBody
	@RequestMapping("check")
	public JsonResponseMsg formCheck(String filename) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (StringUtils.isBlank(filename)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		// 组装form参数
		Map<String, Object> params = Maps.newTreeMap();
		String bucket = UpYunUploaderUtils.BUCKET_NAME;
		params.put("bucket", bucket);

		String path = ImgUploadUtil.genFilePath("/uploads/form");
		String randomFilename = FileUploadUtil.genFileName(filename);
		String saveKey = path + randomFilename;
		params.put("save-key", saveKey);
		long expiration = DateUtils.getTomorrow().getTime();
		params.put("expiration", expiration);

		// 组装响应结果
		HashMap<String, Object> resultMap = Maps.newHashMap();
		String action = UpYunUploaderUtils.FORM_API_URL_AUTO + UpYunUploaderUtils.BUCKET_NAME;
		resultMap.put("action", action);
		resultMap.put("params", params);
		String policy = UpYunUploaderUtils.formPolicy(params);
		resultMap.put("policy", policy);
		String signature = UpYunUploaderUtils.formSignature(policy);
		resultMap.put("signature", signature);
		resultMap.put("filename", filename);

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, resultMap);
	}

}
