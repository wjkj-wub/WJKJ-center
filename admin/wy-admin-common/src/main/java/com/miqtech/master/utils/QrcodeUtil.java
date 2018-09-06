package com.miqtech.master.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import java.util.List;

public class QrcodeUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(QrcodeUtil.class);
	private static final String CHARSET = "utf-8";
	private static final String FORMAT_NAME = "PNG";
	public static final int CHANGCI = 1;
	public static final int SAISHI = 2;
	public static final int ZHANDUI = 3;
	// 二维码尺寸  
	private int qrcode_size = 300;
	// LOGO宽度  
	private int logo_width = 80;
	// LOGO高度  
	private int logo_height = 80;

	private Font font = new Font("SimHei", Font.BOLD, 20);

	private Font topfont = new Font("SimHei", Font.BOLD, 30);
	//LOGO地址 
	private String logopath = "http://img.wangyuhudong.com/wylogo.png";

	private String changciBackground = "http://img.wangyuhudong.com/uploads/imgs/qrcode/background/changci.png";

	private String saishiBackground = "http://img.wangyuhudong.com/uploads/imgs/qrcode/background/saishi.png";

	private String zhanduiBackground = "http://img.wangyuhudong.com/uploads/imgs/qrcode/background/zhandui.png";

	public Font getTopfont() {
		return topfont;
	}

	public void setTopfont(Font topfont) {
		this.topfont = topfont;
	}

	public String getChangciBackground() {
		return changciBackground;
	}

	public void setChangciBackground(String changciBackground) {
		this.changciBackground = changciBackground;
	}

	public String getSaishiBackground() {
		return saishiBackground;
	}

	public void setSaishiBackground(String saishiBackground) {
		this.saishiBackground = saishiBackground;
	}

	public String getZhanduiBackground() {
		return zhanduiBackground;
	}

	public void setZhanduiBackground(String zhanduiBackground) {
		this.zhanduiBackground = zhanduiBackground;
	}

	public QrcodeUtil() {
	}

	public QrcodeUtil(int qrcode_size, int logo_width, int logo_height, Font topfont, Font font) {
		this.logo_height = logo_height;
		this.qrcode_size = qrcode_size;
		this.logo_width = logo_width;
		this.font = font;
		this.topfont = topfont;
	}

	public QrcodeUtil(int qrcode_size, int logo_width, int logo_height, Font font) {
		this.logo_height = logo_height;
		this.qrcode_size = qrcode_size;
		this.logo_width = logo_width;
		this.font = font;
	}

	public QrcodeUtil(int qrcode_size, Font topfont, Font font) {
		this.qrcode_size = qrcode_size;
		this.font = font;
		this.topfont = topfont;
	}

	public QrcodeUtil(int qrcode_size, Font font) {
		this.qrcode_size = qrcode_size;
		this.font = font;
	}

	public String getLogopath() {
		return logopath;
	}

	public void setLogopath(String logopath) {
		this.logopath = logopath;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public int getQrcode_size() {
		return qrcode_size;
	}

	public void setQrcode_size(int qrcode_size) {
		this.qrcode_size = qrcode_size;
	}

	public int getLogo_width() {
		return logo_width;
	}

	public void setLogo_width(int logo_width) {
		this.logo_width = logo_width;
	}

	public int getLogo_height() {
		return logo_width;
	}

	public void setLogo_height(int logo_height) {
		this.logo_height = logo_height;
	}

	private BufferedImage createImage(String content, boolean needCompress) throws Exception {
		return createImage(content, needCompress, true);
	}

	/*生成二维码
	 * */
	private BufferedImage createImage(String content, boolean needCompress, boolean withStyle) throws Exception {
		Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
		// hints.put(EncodeHintType.MARGIN, 1); 
		BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, qrcode_size, qrcode_size,
				hints);
		int width = bitMatrix.getWidth();
		int height = bitMatrix.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int forecolor = withStyle ? 0xFF5F00 : 0x00000000;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, bitMatrix.get(x, y) ? forecolor : 0xFFFFFFFF);
			}
		}
		if (logopath == null || "".equals(logopath)) {
			return image;
		}
		if (withStyle) {
			// 插入图片  
			insertImage(image, needCompress);
		}
		return image;
	}

	/** 
	 * 插入LOGO 
	 *  
	 * @param source 
	 *            二维码图片 
	 * @param needCompress 
	 *            是否压缩 
	 * @throws Exception 
	 */
	private void insertImage(BufferedImage source, boolean needCompress) throws Exception {

		Image src = null;
		if (logopath.startsWith("http")) {
			src = ImageIO.read(new URL(logopath));
		} else {
			File file = new File(logopath);
			if (!file.exists()) {
				System.err.println("" + logopath + "   该文件不存在！");
				return;
			}
			src = ImageIO.read(new File(logopath));
		}
		int width = src.getWidth(null);
		int height = src.getHeight(null);
		if (needCompress) { // 压缩LOGO  
			if (width > logo_width) {
				width = logo_width;
			}
			if (height > logo_height) {
				height = logo_height;
			}

			Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics g = tag.getGraphics();
			g.drawImage(image, 0, 0, null); // 
			g.dispose();
			src = image;
		}
		// 插入LOGO  
		Graphics2D graph = source.createGraphics();
		int x = (qrcode_size - width) / 2;
		int y = (qrcode_size - height) / 2;
		graph.drawImage(src, x, y, null);
		float round = 10;
		float storke = 4f;
		if (width > 90) {
			round = 15;
			storke = 8f;
		}
		Shape shape = new RoundRectangle2D.Float(x, y, width, height, round, round);
		graph.setStroke(new BasicStroke(storke));
		graph.draw(shape);
		graph.dispose();
	}

	/** 
	 * 生成二维码(内嵌LOGO) 
	 *  
	 * @param content 
	 *            内容 
	 * @param destPath 
	 *            存放目录 
	 * @param needCompress 
	 *            是否压缩LOGO 
	 * @throws Exception 
	 */
	public BufferedImage encode(String content, boolean needCompress) throws Exception {
		BufferedImage image = createImage(content, needCompress);
		//ImageIO.write(image, FORMAT_NAME, new File(destFileFullPath));  
		return image;
	}

	/**
	 * 生成不带样式的二维码
	 */
	public BufferedImage encodeSimpleImage(String content) throws Exception {
		return createImage(content, false, false);
	}

	/** 
	 * 当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常) 
	 *  
	 *            存放目录 
	 */
	public static void mkdirs(String destPath) {
		File file = new File(destPath);
		// 当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常)  
		if (!file.exists() && !file.isDirectory()) {
			file.mkdirs();
		}
	}

	/** 
	 * 生成二维码(内嵌LOGO) 
	 *  
	 * @param content 
	 *            内容 
	 * @param destPath 
	 *            存储地址 
	 * @throws Exception 
	 */
	public BufferedImage encode(String content) throws Exception {
		return encode(content, false);
	}

	/** 
	 * 生成二维码(内嵌LOGO) 
	 *  
	 * @param content 
	 *            内容 
	 * @param output 
	 *            输出流 
	 * @param needCompress 
	 *            是否压缩LOGO 
	 * @throws Exception 
	 */
	public void encode(String content, OutputStream output, boolean needCompress) throws Exception {
		BufferedImage image = createImage(content, needCompress);
		ImageIO.write(image, FORMAT_NAME, output);
	}

	/** 
	 * 生成二维码 
	 *  
	 * @param content 
	 *            内容 
	 * @param output 
	 *            输出流 
	 * @throws Exception 
	 */
	public void encode(String content, OutputStream output) throws Exception {
		encode(content, output, false);
	}

	/** 
	 * 解析二维码 
	 *  
	 * @param file 
	 *            二维码图片 
	 * @return 
	 * @throws Exception 
	 */
	public static String decode(File file) throws Exception {
		BufferedImage image;
		image = ImageIO.read(file);
		if (image == null) {
			return null;
		}
		BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Result result;
		Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
		hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
		result = new MultiFormatReader().decode(bitmap, hints);
		String resultStr = result.getText();
		return resultStr;
	}

	/** 
	 * 解析二维码 
	 *  
	 * @param path 
	 *            二维码图片地址 
	 * @return 
	 * @throws Exception 
	 */
	public static String decode(String path) throws Exception {
		return QrcodeUtil.decode(new File(path));
	}

	//根据str,font的样式以及输出文件目录
	public BufferedImage addRoundWordImage(String[][] str) throws Exception {
		int x = qrcode_size / 9;
		int half = qrcode_size / str.length;
		int valuesize = str.length == 1 ? qrcode_size / 10 * 8 : half - x - 20;
		int maxrow = 0;
		List<Object[]> draws = new ArrayList<Object[]>();
		Object[] valuespace;
		Rectangle2D r1 = font.getStringBounds("1",
				new FontRenderContext(AffineTransform.getScaleInstance(1, 1), false, false));
		int unitHeight = (int) Math.floor(r1.getHeight());//获取单个字符的高度
		for (int i = 0; i < str.length; i++) {
			int row = 0;
			for (int j = 0; j < str[i].length; j++) {
				Rectangle2D r = font.getStringBounds(str[i][j],
						new FontRenderContext(AffineTransform.getScaleInstance(1, 1), false, false));
				int width = (int) Math.floor(r.getWidth());//获取字符宽度
				int height = (int) Math.floor(r.getHeight());//获取字符高度
				int codeCount = str[i][j].length();
				if (valuesize < width) {
					int codewidth = width / codeCount;
					int index = codeCount;
					while (valuesize < codewidth * index) {
						index--;
					}
					String start = str[i][j].substring(0, index);
					String end = str[i][j].substring(index, str[i][j].length());
					valuespace = getObjectArr(start, i == 0 ? x : half * i, height * (1 + row++));
					draws.add(valuespace);
					valuespace = getObjectArr(end, i == 0 ? x : half * i, height * (1 + row++));
					draws.add(valuespace);
				} else {
					valuespace = getObjectArr(str[i][j], i == 0 ? x : half * i, height * (1 + row++));
					draws.add(valuespace);
				}
			}
			if (maxrow < row) {
				maxrow = row;
			}
		}
		BufferedImage image = new BufferedImage(qrcode_size, unitHeight * maxrow + 20, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, qrcode_size, unitHeight * maxrow + 20);
		g.setColor(Color.black);//在换成黑色
		g.setFont(font);//设置画笔字体
		//画出字符串
		for (int k = 0; k < draws.size(); k++) {
			Object[] spacevalue = draws.get(k);
			g.drawString(spacevalue[0] + "", (Integer) spacevalue[1], (Integer) spacevalue[2]);
		}

		return image;
	}

	//根据str,font的样式以及输出文件目录
	public BufferedImage addTopWordImage(String[] str) throws Exception {
		List<Object[]> draws = new ArrayList<Object[]>();
		Object[] valuespace;
		Rectangle2D r1 = topfont.getStringBounds("1",
				new FontRenderContext(AffineTransform.getScaleInstance(1, 1), false, false));
		int unitHeight = (int) Math.floor(r1.getHeight());//获取单个字符的高度
		for (int i = 0; i < str.length; i++) {

			Rectangle2D r = topfont.getStringBounds(str[i],
					new FontRenderContext(AffineTransform.getScaleInstance(1, 1), false, false));
			int width = (int) Math.floor(r.getWidth());//获取字符宽度
			System.out.println(unitHeight * (1 + i) * (i > 0 ? 20 : 10) / 10);
			int height = unitHeight * (1 + i) * 4 / 3;
			valuespace = getObjectArr(str[i], (qrcode_size - width) / 2, height);
			draws.add(valuespace);
		}
		int length = (4 * unitHeight / 3) * str.length;

		BufferedImage image = new BufferedImage(qrcode_size, length + 20, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, qrcode_size, length + 20);
		g.setColor(Color.black);//在换成黑色
		g.setFont(topfont);//设置画笔字体
		//画出字符串
		for (int k = 0; k < draws.size(); k++) {
			Object[] spacevalue = draws.get(k);
			g.drawString(spacevalue[0] + "", (Integer) spacevalue[1], (Integer) spacevalue[2]);
		}
		return image;
	}

	public Object[] getObjectArr(String value, int width, int height) {
		Object[] valuespace = new Object[3];
		valuespace[0] = value;
		valuespace[1] = width;
		valuespace[2] = height;
		return valuespace;
	}

	//

	public String drawFixedPicture(String content, String[] msgs, String destFileName, String destPath) {
		this.qrcode_size = 484;
		this.logo_width = 80;
		this.logo_height = 80;
		Font topFixfont = new Font("SimHei", Font.PLAIN, 25);
		Font msgFixfont = new Font("SimHei", Font.PLAIN, 25);
		Font endFixfont = new Font("SimHei", Font.PLAIN, 22);
		BufferedImage image = null;
		Graphics g = null;
		BufferedImage qrImage = null;
		String path = "";
		try {
			if (msgs.length < 3) {
				return "";
			}
			image = new BufferedImage(qrcode_size, 584, BufferedImage.TYPE_INT_BGR);
			g = image.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, qrcode_size, 584);
			qrImage = encode(content, true);
			g.drawImage(qrImage, 0, 60, null);
			g.setColor(new Color(51, 51, 51));//在换成黑色
			g.setFont(topFixfont);//设置画笔字体	
			Rectangle2D r1 = topFixfont.getStringBounds(msgs[0],
					new FontRenderContext(AffineTransform.getScaleInstance(1, 1), false, false));
			int width = (int) r1.getWidth();
			Rectangle2D r2 = msgFixfont.getStringBounds(msgs[1],
					new FontRenderContext(AffineTransform.getScaleInstance(1, 1), false, false));
			int width2 = (int) r2.getWidth();
			Rectangle2D r3 = endFixfont.getStringBounds(msgs[2],
					new FontRenderContext(AffineTransform.getScaleInstance(1, 1), false, false));
			int width3 = (int) r3.getWidth();
			g.drawString(msgs[0], (qrcode_size - width) / 2, 48);
			g.setColor(new Color(255, 95, 0));//在换成橙色
			g.setFont(msgFixfont);//设置画笔字体	

			g.drawString(msgs[1], (qrcode_size - width2) / 2, 88);
			g.setColor(new Color(102, 102, 102));//在换成色
			g.setFont(endFixfont);//设置画笔字体	
			g.drawString(msgs[2], (qrcode_size - width3) / 2, 530);
			path = destPath + destFileName + ".png";
			ImageIO.write(image, FORMAT_NAME, new File(path));
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("生成二维码图片异常：", e);
		} finally {
			if (g != null) {
				g.dispose();
			}
		}

		return path;
	}

	public static void main(String[] args) throws Exception {
		//      String text = "www.baidu.com";  
		//	JSONObject json = new JSONObject();  
		QrcodeUtil zu = new QrcodeUtil();
		//zu.setLogopath("http://img.wangyuhudong.com/wy_share_icon.png");
		String[][] msg = { { "网娱大师比赛战队邀请", "熊猫战队熊猫战队熊猫战队熊", "该二维码将在2016年5月3日比赛结束时失效" }, {} };
		//zu.drawFixedPicture("ddadadadaffe3re", msg, "fixqrcode", "D:/tools/二维码/");
		zu.setQrcode_size(1000);
		//		zu.setLogo_height(150);
		//		zu.setLogo_width(150);
		//zu.createImageAndWord("111111111dadafafvafafcfaf1131313131", msg, "testew2", "D:/tools/二维码/");
		zu.createImageWithBackground(msg[0][2], "backqrcode2", "D:/tools/二维码/", QrcodeUtil.SAISHI);
		//		zu.createImageWithBackground(msg[2], "backqrcode3", "D:/tools/二维码/", QrcodeUtil.ZHANDUI);
		//		String destFileName = "q2code" + ".png";
		//		String destFileFullPath = "D:/tools/二维码/" + destFileName;
		//		String decodeText = QrcodeUtil.decode(destFileFullPath);
		//		System.out.println("decodeText = " + decodeText);
	}

	//二维码带字（上下）图片生成
	public String createImageAndWord(String content, String[] topinfo, String[][] activityInfo, String destFileName,
			String destPath) {
		Graphics graphics = null;
		BufferedImage image = null;
		BufferedImage wordimage = null;
		BufferedImage topwordimage = null;
		String destFileFullPath = "";
		try {
			if (topinfo.length > 0) {
				System.out.println(topinfo[1]);
				topwordimage = addTopWordImage(topinfo);
			}
			if (activityInfo.length > 0) {
				wordimage = addRoundWordImage(activityInfo);
			}
			destFileName = destFileName + ".png";
			destFileFullPath = destPath + "/" + destFileName;
			//生成带logo的二维码
			image = encode(content, true);
			mkdirs(destPath);
			//背景图
			int removesize = qrcode_size / 20;
			BufferedImage balckGroundImage = new BufferedImage(qrcode_size,
					qrcode_size + (wordimage == null ? 0 : wordimage.getHeight() * 6 / 5)
							+ (topwordimage == null ? 0 : topwordimage.getHeight() * 6 / 5) - removesize,
					BufferedImage.TYPE_INT_RGB);
			graphics = balckGroundImage.getGraphics();
			//先用白色填充整张图片,也就是背景
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, balckGroundImage.getWidth(), balckGroundImage.getHeight());

			graphics.drawImage(image, 0, topwordimage.getHeight() - removesize, null);
			if (topwordimage != null) {
				graphics.drawImage(topwordimage, 0, 0, null);
			}
			//增加说明文字     
			if (wordimage != null) {
				graphics.drawImage(wordimage, 0, topwordimage.getHeight() + qrcode_size - 2 * removesize, null);
			}
			ImageIO.write(balckGroundImage, FORMAT_NAME, new File(destFileFullPath));
		} catch (Exception e) {
			LOGGER.error("生成二维码图片异常：", e);
		} finally {
			if (graphics != null) {
				graphics.dispose();
			}
		}
		return destFileFullPath;
	}

	//二维码带字图片生成
	public String createImageAndWord(String content, String[][] activityInfo, String destFileName, String destPath) {
		Graphics graphics = null;
		BufferedImage image = null;
		BufferedImage wordimage = null;
		String destFileFullPath = "";
		try {
			if (activityInfo != null && activityInfo.length > 0) {
				wordimage = addRoundWordImage(activityInfo);
			}
			destFileName = destFileName + ".png";
			destFileFullPath = destPath + "/" + destFileName;
			//生成带logo的二维码
			image = encode(content, true);
			mkdirs(destPath);
			//背景图
			int removesize = qrcode_size / 20;
			BufferedImage balckGroundImage = new BufferedImage(qrcode_size,
					qrcode_size + (wordimage == null ? 0 : wordimage.getHeight() * 6 / 5) - removesize,
					BufferedImage.TYPE_INT_RGB);
			graphics = balckGroundImage.getGraphics();
			//先用白色填充整张图片,也就是背景
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, balckGroundImage.getWidth(), balckGroundImage.getHeight());
			graphics.drawImage(image, 0, 0, null);
			//增加说明文字     
			if (wordimage != null) {
				graphics.drawImage(wordimage, 0, qrcode_size - removesize, null);
			}
			ImageIO.write(balckGroundImage, FORMAT_NAME, new File(destFileFullPath));
		} catch (Exception e) {
			LOGGER.error("生成二维码图片异常：", e);
		} finally {
			if (graphics != null) {
				graphics.dispose();
			}
		}
		return destFileFullPath;
	}

	public String createImageWithBackground(String content, String destFileName, String destPath, int type) {
		return createImageWithBackground(content, destFileName, destPath, type, 0, 0);
	}

	//二维码套图图片生成
	public String createImageWithBackground(String content, String destFileName, String destPath, int type, int w,
			int h) {
		Graphics graphics = null;
		BufferedImage image = null;
		BufferedImage backImage = null;
		String destFileFullPath = "";
		this.qrcode_size = 360;
		try {
			destFileName = destFileName + ".png";
			destFileFullPath = destPath + "/" + destFileName;
			//生成带logo的二维码
			image = encode(content, true);
			mkdirs(destPath);
			switch (type) {
			case CHANGCI:
				backImage = ImageIO.read(new URL(changciBackground));
				break;
			case SAISHI:
				backImage = ImageIO.read(new URL(saishiBackground));
				break;
			case ZHANDUI:
				backImage = ImageIO.read(new URL(zhanduiBackground));
				break;
			default:
				break;
			}
			//背景图
			BufferedImage balckGroundImage = new BufferedImage(900, 900, BufferedImage.TYPE_INT_RGB);
			graphics = balckGroundImage.getGraphics();
			//先用白色填充整张图片,也就是背景
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, 900, 900);
			graphics.drawImage(backImage, 50, 50, null);
			//去除二维码白色区域     
			java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
			ImageIO.write(image, FORMAT_NAME, output);
			byte[] buff = output.toByteArray();
			java.io.InputStream is = new java.io.ByteArrayInputStream(buff);
			ImageUtil imageUtil = new ImageUtil();
			image = imageUtil.cutImage(is, 30, 30, 300, 300);
			graphics.drawImage(image, 300, 300, null);
			if (w < 800 && w > 0 && h > 0 && h < 800) {
				balckGroundImage = imageUtil.thumbnailImage(balckGroundImage, w, h);
			}
			ImageIO.write(balckGroundImage, FORMAT_NAME, new File(destFileFullPath));
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("生成二维码图片异常：", e);
		} finally {
			if (graphics != null) {
				graphics.dispose();
			}
		}
		return destFileFullPath;
	}

	//二维码图片生成高、宽一致的
	public String createImageAndWord(String content, String destFileName, String destPath) {
		Graphics graphics = null;
		BufferedImage image = null;
		String destFileFullPath = "";
		try {
			destFileName = destFileName + ".png";
			destFileFullPath = destPath + "/" + destFileName;
			//生成带logo的二维码
			image = encode(content, true);
			mkdirs(destPath);
			BufferedImage balckGroundImage = new BufferedImage(qrcode_size, qrcode_size, BufferedImage.TYPE_INT_RGB);
			graphics = balckGroundImage.getGraphics();
			//先用白色填充整张图片,也就是背景
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, balckGroundImage.getWidth(), balckGroundImage.getHeight());
			graphics.drawImage(image, 0, 0, null);
			ImageIO.write(balckGroundImage, FORMAT_NAME, new File(destFileFullPath));
		} catch (Exception e) {
			LOGGER.error("生成二维码图片异常：", e);
		} finally {
			if (graphics != null) {
				graphics.dispose();
			}
		}
		return destFileFullPath;
	}

}
