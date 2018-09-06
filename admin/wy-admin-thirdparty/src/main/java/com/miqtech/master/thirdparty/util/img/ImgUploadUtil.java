package com.miqtech.master.thirdparty.util.img;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.simpleimage.ImageFormat;
import com.alibaba.simpleimage.ImageRender;
import com.alibaba.simpleimage.SimpleImageException;
import com.alibaba.simpleimage.render.CropParameter;
import com.alibaba.simpleimage.render.CropRender;
import com.alibaba.simpleimage.render.ReadRender;
import com.alibaba.simpleimage.render.ScaleParameter;
import com.alibaba.simpleimage.render.ScaleRender;
import com.alibaba.simpleimage.render.WriteParameter;
import com.alibaba.simpleimage.render.WriteRender;
import com.google.common.collect.Maps;
import com.miqtech.master.thirdparty.util.UpYunUploaderUtils;
import com.miqtech.master.utils.QrcodeUtil;

/**
 * 图片上传工具
 */
public class ImgUploadUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImgUploadUtil.class);

	private static final String FORMAT_NAME = "PNG";

	private static final String PATH_UPLOAD = "uploads/";
	private static final String PATH_IMG = "imgs/";

	public static final String THUMB_IMG_SUFFIX = "!small";//又拍云支持的小图后缀
	public static final String MEDIA_IMG_SUFFIX = "!middle";//又拍云支持的中图后缀

	public static final String KEY_MAP_SRC = "SRC_IMG";// 原图
	public static final String KEY_MAP_THUMB = "THUMB_IMG";// 缩略图
	public static final String KEY_MAP_MEDIA = "MEDIA_IMG";// 中图

	/**
	 * 获取图片上传的访问路径
	 * @param category:路径类别
	 */
	public static String genFilePath(String category) {
		return category + "/" + new SimpleDateFormat("YYYY" + "/" + "MM" + "/" + "dd" + "/" + "")
				.format(Calendar.getInstance().getTime());// 图片保存路径
	}

	/**
	 * 图片上传又拍云
	 */
	/*
	public static Map<String, String> save(MultipartFile srcFile, String systemName, String src) {
		Map<String, String> result = new HashMap<String, String>();
		if (src == null) {
			src = "";
		}
		String localTmpDir = "/" + "tmp" + "/" + systemName + "/";
		// 上传原图
		String fileName = FileUploadUtil.genFileName(srcFile.getOriginalFilename());
		String path = PATH_UPLOAD + PATH_IMG + src;
		File targetFile = FileUploadUtil.createFile(localTmpDir + path + fileName);
		try {
			srcFile.transferTo(targetFile);
			UpYunUploaderUtils.uploadImg(targetFile, path, fileName);
			String srcPicUrl = FileUploadUtil.getFrontpathByFilepath(path + fileName);
			result.put(KEY_MAP_SRC, srcPicUrl);
			result.put(KEY_MAP_MEDIA, srcPicUrl + MEDIA_IMG_SUFFIX);
			result.put(KEY_MAP_THUMB, srcPicUrl + THUMB_IMG_SUFFIX);
		} catch (Exception e) {
			LOGGER.error("上传图片异常：", e);
		}
	
		return result;
	}*/

	/**
	 * 截图处理并上传又拍云
	 */
	public static Map<String, String> cropAndUploadUpyun(MultipartFile srcFile, int imageX, int imageY, int imageW,
			int imageH, String systemName, String src) {
		Map<String, String> result = new HashMap<String, String>();
		if (src == null) {
			src = "";
		}
		String localTmpDir = "/" + "tmp" + "/" + systemName + "/";
		// 上传原图
		String fileName = FileUploadUtil.genFileName(srcFile.getOriginalFilename());
		String path = PATH_UPLOAD + PATH_IMG + src;
		File tmpFile = FileUploadUtil.createFile(localTmpDir + path + fileName);
		//输出文件
		File cutFile = FileUploadUtil
				.createFile(localTmpDir + path + FileUploadUtil.genFileName(srcFile.getOriginalFilename()));
		try {
			srcFile.transferTo(tmpFile);

			FileInputStream inStream = null;
			FileOutputStream outStream = null;
			CropRender cr = null;
			ImageRender wr = null;
			try {
				inStream = new FileInputStream(tmpFile);
				outStream = new FileOutputStream(cutFile);
				CropParameter params = new CropParameter(imageX, imageY, imageW, imageH);
				cr = new CropRender(inStream, params);
				wr = new WriteRender(cr, outStream);
				wr.render();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(inStream); //图片文件输入输出流必须记得关闭
				IOUtils.closeQuietly(outStream);
				if (cr != null) {
					try {
						cr.dispose(); //释放simpleImage的内部资源
					} catch (SimpleImageException ignore) {
					}
				}
			}
			UpYunUploaderUtils.uploadImg(cutFile, path, fileName);
			String srcPicUrl = FileUploadUtil.getFrontpathByFilepath(path + fileName);
			result.put(KEY_MAP_SRC, srcPicUrl);
			result.put(KEY_MAP_MEDIA, srcPicUrl + MEDIA_IMG_SUFFIX);
			result.put(KEY_MAP_THUMB, srcPicUrl + THUMB_IMG_SUFFIX);
		} catch (Exception e) {
			LOGGER.error("上传图片异常：", e);
		}
		return result;
	}

	/**
	 * 压缩图片并上传又拍云：图片大于1M就按原尺寸进行压缩
	 */
	public static Map<String, String> save(MultipartFile srcFile, String sysName, String src) {
		return save(srcFile, sysName, src, false);
	}

	public static Map<String, String> save(MultipartFile srcFile, String sysName, String src, boolean retainFileName) {
		if (src == null) {
			src = StringUtils.EMPTY;
		}

		// 产生临时文件
		String localTempDir = "/" + "tmp" + "/" + sysName + "/";
		String path = PATH_UPLOAD + PATH_IMG + src;
		String fileName = null;
		String originalFileName = srcFile.getOriginalFilename();
		if (retainFileName) {// 保留文件名作为前缀
			String baseName = FilenameUtils.getBaseName(originalFileName);
			fileName = baseName + "_" + FileUploadUtil.genFileName(originalFileName);
		} else {
			fileName = FileUploadUtil.genFileName(originalFileName);
		}
		File tempFile = FileUploadUtil.createFile(localTempDir + path + fileName);
		try {
			srcFile.transferTo(tempFile);
		} catch (IOException e) {
			LOGGER.error("图片异常：", e);
		}

		// 保存图片
		return save(tempFile, sysName, src, retainFileName);
	}

	public static Map<String, String> save(File file, String sysName, String src, boolean retainFileName) {
		// 产生保存路径
		String path = PATH_UPLOAD + PATH_IMG + src;
		String fileName = null;
		String originalFileName = file.getName();
		if (retainFileName) {// 保留文件名作为前缀
			String baseName = FilenameUtils.getBaseName(originalFileName);
			fileName = baseName + "_" + FileUploadUtil.genFileName(originalFileName);
		} else {
			fileName = FileUploadUtil.genFileName(originalFileName);
		}

		if (file.length() > 1024 * 1024) { //大于0.5M则进行压缩
			//压缩图
			String localTempDir = "/" + "tmp" + "/" + sysName + "/";
			File outFile = FileUploadUtil.createFile(localTempDir + path + fileName + "_out");

			FileInputStream inStream = null;
			FileOutputStream outStream = null;
			WriteRender writeRender = null;
			try {
				inStream = new FileInputStream(file);
				outStream = new FileOutputStream(outFile);
				Image image = ImageIO.read(file);
				int width = image.getWidth(null);
				int height = image.getHeight(null);
				ScaleParameter scaleParam = new ScaleParameter(width, height); //按原尺寸压缩
				ImageRender readRender = new ReadRender(inStream);
				ImageRender scaleRender = new ScaleRender(readRender, scaleParam);
				WriteParameter writeParam = new WriteParameter();
				writeParam.setDefaultQuality(0.75F); //压缩质量参数，可修改
				writeRender = new WriteRender(scaleRender, outStream, ImageFormat.JPEG, writeParam);

				writeRender.render(); //触发图像处理
			} catch (Exception e) {
				LOGGER.error("压缩图片异常：", e);
			} finally {
				IOUtils.closeQuietly(inStream); //关闭图片文件输入输出流
				IOUtils.closeQuietly(outStream);
				if (writeRender != null) {
					try {
						writeRender.dispose(); //释放simpleImage的内部资源
					} catch (SimpleImageException e) {
						LOGGER.error("压缩图片释放资源异常：", e);
					}
				}
			}
			UpYunUploaderUtils.uploadImg(outFile, path, fileName);
		} else {
			UpYunUploaderUtils.uploadImg(file, path, fileName); //不处理直接上传
		}

		// 产生返回结果
		String srcPicUrl = FileUploadUtil.getFrontpathByFilepath(path + fileName);
		Map<String, String> result = Maps.newHashMap();
		result.put(KEY_MAP_SRC, srcPicUrl);
		result.put(KEY_MAP_MEDIA, srcPicUrl + MEDIA_IMG_SUFFIX);
		result.put(KEY_MAP_THUMB, srcPicUrl + THUMB_IMG_SUFFIX);
		return result;
	}

	public static Map<String, String> uploadQrcodePath(String content, String[] msgs, String systemName, String src,
			QrcodeUtil qu) {
		Map<String, String> imgPaths = null;
		String temp = "/" + "tmp" + "/" + systemName;
		String filepath = qu.createImageWithBackground(content, "/temp", temp, QrcodeUtil.ZHANDUI);
		File uploadFile = new File(filepath);
		if (uploadFile != null) {// 有图片时上传文件
			imgPaths = ImgUploadUtil.save(uploadFile, systemName, src, false);
		}
		return imgPaths;
	}

	/**
	 * 产生并上传一张简单二维码图片
	 */
	public static String uploadSingleQrcode(String content, String systemName, String src) {
		try {
			// 产生二维码
			QrcodeUtil qu = new QrcodeUtil();
			BufferedImage qrcode = qu.encodeSimpleImage(content);

			// 转换图片为临时文件
			String filename = FileUploadUtil.genFileName(".png");
			String tmpFilePath = "/tmp/" + systemName + "/" + filename;
			File qrcodeFile = new File(tmpFilePath);
			if (!qrcodeFile.exists()) {
				qrcodeFile.createNewFile();
			}
			ImageIO.write(qrcode, FORMAT_NAME, qrcodeFile);

			// 上传临时文件到又拍云
			if (qrcodeFile != null) {
				Map<String, String> paths = save(qrcodeFile, systemName, src, false);
				if (MapUtils.isNotEmpty(paths)) {
					return MapUtils.getString(paths, KEY_MAP_SRC);
				}
			}
		} catch (Exception e) {
			LOGGER.error("产生二维码异常:", e);
		}
		return null;
	}
}
