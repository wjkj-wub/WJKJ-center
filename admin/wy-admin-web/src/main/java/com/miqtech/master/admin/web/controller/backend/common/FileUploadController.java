package com.miqtech.master.admin.web.controller.backend.common;

import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.thirdparty.util.UpYunUploaderUtils;
import com.miqtech.master.utils.IdentityUtils;
import com.miqtech.master.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("common/")
public class FileUploadController extends BaseController {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadController.class);

	private static final String IMG_SERVER = "http://img.wangyuhudong.com/";
	private static final String LOCAL_TMP_DIR = "/" + "tmp" + "/";

	@RequestMapping(value = "upload", method = RequestMethod.POST)
	@ResponseBody
	public String upload(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>(16);
		boolean success = false;
		String msg = "上传失败信息";
		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
		//取得request中的所有文件名
		Iterator<String> iter = multiRequest.getFileNames();
		while (iter.hasNext()) {
			//取得上传文件
			MultipartFile file = multiRequest.getFile(iter.next());
			if (file != null && !file.isEmpty()) {
				String fileUrl = uploadToUpYun(file);
				if (StringUtils.isNotBlank(fileUrl)) {
					success = true;
					msg = "上传成功";
				}
				result.put("success", success);
				result.put("msg", msg);
				result.put("file_path", fileUrl);
				break;
			}
		}
		return JsonUtils.objectToString(result);
	}

	public String uploadToUpYun(MultipartFile srcFile) {
		String typeSplitter = ".";
		String type = StringUtils.substringAfterLast(srcFile.getOriginalFilename(), typeSplitter);
		String fileName;
		if (StringUtils.isBlank(type)) {
			fileName = IdentityUtils.uuidWithoutSplitter();
		} else {
			fileName = IdentityUtils.uuidWithoutSplitter() + typeSplitter + type;
		}
		String genDir = genDir(new Date());
		String url = IMG_SERVER + genDir + fileName;
		try {
			File targetFile = createFile(LOCAL_TMP_DIR + fileName);
			srcFile.transferTo(targetFile);
			return UpYunUploaderUtils.uploadImg(targetFile, genDir, fileName) ? url : null;
		} catch (IllegalStateException e) {
			LOGGER.error("上传图片到又拍云异常", e);
		} catch (IOException e) {
			LOGGER.error("上传图片到又拍云异常", e);
		}

		return null;
	}

	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"YYYY" + "/" + "MM" + "/" + "dd" + "/" + "");

	private String genDir(Date time) {
		String dir = "/uploads/UEditor" + "/" + simpleDateFormat.format(time);// 图片保存路径
		return dir;
	}

	public File createFile(String dir) {
		File f = new File(dir);
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		return f;
	}
}