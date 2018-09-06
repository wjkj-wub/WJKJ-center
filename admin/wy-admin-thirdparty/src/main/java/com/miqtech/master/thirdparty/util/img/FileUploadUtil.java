package com.miqtech.master.thirdparty.util.img;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.miqtech.master.thirdparty.util.UpYunUploaderUtils;
import com.miqtech.master.utils.IdentityUtils;

/**
 * 文件上传工具
 */
public class FileUploadUtil {

	private static final String PATH_UPLOAD = "uploads/";

	/**
	 * 拷贝源文件到目标文件
	 */
	public static boolean copy(File srcFile, File targetFile) throws IOException {
		FileInputStream fin = new FileInputStream(srcFile);
		FileOutputStream fout = new FileOutputStream(targetFile);
		FileChannel fcin = fin.getChannel();
		FileChannel fcout = fout.getChannel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		while (true) {
			buffer.clear();
			int r = fcin.read(buffer);
			if (r == -1) {
				break;
			}
			buffer.flip();
			fcout.write(buffer);
		}

		fcout.close();
		fcin.close();
		fout.close();
		fin.close();

		return true;
	}

	/**
	 * 产生以时间开头的随机字符串文件名，并拼接上附加部分
	 */
	public static String genFileName(String oldName) {
		String oldExt = FilenameUtils.getExtension(oldName);
		if (StringUtils.isEmpty(oldExt)) {
			return new StringBuffer().append(IdentityUtils.uuidWithoutSplitter()).toString();
		}
		String extension = "." + (StringUtils.isEmpty(oldExt) ? "" : oldExt).toLowerCase();// 获取文件后缀
		return new StringBuffer().append(IdentityUtils.uuidWithoutSplitter()).append(extension).toString();// 拼接后缀
	}

	/**
	 * 创建文件，并检查父级目录是否存在
	 */
	public static File createFile(String dir) {
		File f = new File(dir);
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		return f;
	}

	/**
	 * 将服务器文件地址 转换为 URL访问地址
	 */
	public static String getFrontpathByFilepath(String filePath) {
		return (filePath).replace(File.separator, "/");
	}

	/**
	 * 上传文件到又拍云上
	 */
	public static String updateToUpYun(MultipartFile srcFile, String sysName, String src) {
		return updateToUpYun(srcFile, sysName, src, false);
	}

	/**
	 * 上传文件到又拍云上
	 */
	public static String updateToUpYun(MultipartFile srcFile, String sysName, String src, boolean retainFileName) {
		if (src == null) {
			src = StringUtils.EMPTY;
		}

		String fileName = StringUtils.EMPTY;
		if (retainFileName) {// 保留文件名作为前缀
			String originalFileName = srcFile.getOriginalFilename();
			String baseName = FilenameUtils.getBaseName(originalFileName);
			fileName = baseName + "_" + FileUploadUtil.genFileName(originalFileName);
		} else {
			fileName = FileUploadUtil.genFileName(srcFile.getOriginalFilename());
		}

		String localTempDir = "/" + "tmp" + "/" + sysName + "/";
		String path = PATH_UPLOAD + "file/" + src;

		File tempFile = FileUploadUtil.createFile(localTempDir + path + fileName);
		try {
			srcFile.transferTo(tempFile);
		} catch (Exception e) {

		}
		UpYunUploaderUtils.uploadImg(tempFile, path, fileName); //不处理直接上传

		String srcPicUrl = FileUploadUtil.getFrontpathByFilepath(path + fileName);
		return srcPicUrl;
	}
}
