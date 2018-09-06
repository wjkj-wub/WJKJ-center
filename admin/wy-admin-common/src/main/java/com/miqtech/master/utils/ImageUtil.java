package com.miqtech.master.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class ImageUtil {

	private static final Logger log = LoggerFactory.getLogger(ImageUtil.class);

	public static BufferedImage createImage(String code) {
		int width = code.length() * 15;
		int height = 20;
		BufferedImage image = new BufferedImage(width, height, 1);
		Graphics g = image.getGraphics();

		g.drawRect(0, 0, width, height);
		g.setColor(Color.WHITE);
		g.fillRect(1, 1, width, height);

		g.setFont(new Font("Times New Roman", 0, 18));
		for (int i = 0; i < code.length(); i++) {
			String rand = code.substring(i, i + 1);

			g.setColor(new Color(48, 76, 129));
			g.drawString(rand, 13 * i + 6, 16);
		}
		Random random = new Random();
		for (int i = 0; i < code.length() * 15; i++) {
			g.setColor(getRandColor(100, 255));
			int x = random.nextInt(width - 2) + 1;
			int y = random.nextInt(height - 2) + 1;
			g.drawRect(x, y, 0, 0);
		}
		g.dispose();
		return image;
	}

	private static Color getRandColor(int fc, int bc) {
		Random random = new Random();
		if (fc > 255) {
			fc = 255;
		}
		if (bc > 255) {
			bc = 255;
		}
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

	private static Boolean DEFAULT_FORCE = false;

	/**
	 * <p>Title: cutImage</p>
	 * <p>Description:  根据原图与裁切size截取局部图片</p>
	 * @param srcImg    源图片
	 * @param output    图片输出流
	 * @param rect        需要截取部分的坐标和大小
	 */
	public BufferedImage cutImage(InputStream srcImg, java.awt.Rectangle rect) {
		ImageInputStream iis = null;
		BufferedImage bi = null;
		try {
			// 将FileInputStream 转换为ImageInputStream
			iis = ImageIO.createImageInputStream(srcImg);
			// 根据图片类型获取该种类型的ImageReader
			ImageReader reader = ImageIO.getImageReadersBySuffix("png").next();
			reader.setInput(iis, true);
			ImageReadParam param = reader.getDefaultReadParam();
			param.setSourceRegion(rect);
			bi = reader.read(0, param);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (srcImg != null) {
					srcImg.close();
				}
				if (iis != null) {
					iis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bi;

	}

	public BufferedImage cutImage(InputStream srcImg, int x, int y, int width, int height) {
		return cutImage(srcImg, new java.awt.Rectangle(x, y, width, height));
	}

	/**
	 * <p>Title: thumbnailImage</p>
	 * <p>Description: 根据图片路径生成缩略图 </p>
	 * @param imagePath    原图片路径
	 * @param w            缩略图宽
	 * @param h            缩略图高
	 * @param force        是否强制按照宽高生成缩略图(如果为false，则生成最佳比例缩略图)
	 */
	public BufferedImage thumbnailImage(BufferedImage srcImg, int w, int h, boolean force) {
		BufferedImage bi = null;
		try {
			// 根据原图与要求的缩略图比例，找到最合适的缩略图比例
			if (!force) {
				int width = srcImg.getWidth(null);
				int height = srcImg.getHeight(null);
				if ((width * 1.0) / w < (height * 1.0) / h) {
					if (width > w) {
						h = Integer.parseInt(new java.text.DecimalFormat("0").format(height * w / (width * 1.0)));
					}
				} else {
					if (height > h) {
						w = Integer.parseInt(new java.text.DecimalFormat("0").format(width * h / (height * 1.0)));
					}
				}
			}
			bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics g = bi.getGraphics();
			g.drawImage(srcImg, 0, 0, w, h, Color.LIGHT_GRAY, null);
			g.dispose();
		} catch (Exception e) {
			log.error("压缩图片失败.", e);
		}
		return bi;
	}

	public BufferedImage thumbnailImage(BufferedImage srcImg, int w, int h) {
		return thumbnailImage(srcImg, w, h, DEFAULT_FORCE);
	}

	public static void main(String[] args) {
		//new ImageUtil().cutImage("D:/tools/二维码/427tqecode.png", "D:/tools/二维码/", 20, 20, 260, 260);
	}

}
