package com.miqtech.master.utils;

import jxl.CellView;
import jxl.Workbook;
import jxl.write.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Number;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Excel操作类
 */
@SuppressWarnings("deprecation")
public class ExcelUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelUtils.class);

	/**
	 * 导出Excel
	 */
	public static void exportExcel(String title, String[][] contents, HttpServletResponse res)
			throws IOException, WriteException {
		exportExcel(title, contents, true, res);
	}

	/**
	 * 导出Excel
	 */
	public static void exportExcel(String title, String[][] contents, boolean numFormat, HttpServletResponse res)
			throws IOException, WriteException {
		OutputStream os = res.getOutputStream();
		String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + title
				+ ".xls";
		res.setHeader("Content-Disposition",
				"attachment;filename=" + new String(fileName.getBytes("gb2312"), "ISO8859-1"));
		res.setContentType("application/msexcel");

		// 创建Excel工作薄
		WritableWorkbook wwb = Workbook.createWorkbook(os);

		// 添加第一个工作表并设置第一个Sheet的名字
		WritableSheet sheet = wwb.createSheet(title, 0);

		// 标题样式
		WritableFont wf_title = new WritableFont(WritableFont.TIMES, 16, WritableFont.BOLD, false);
		WritableCellFormat wcf_title = new WritableCellFormat(wf_title);
		wcf_title.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
		wcf_title.setAlignment(Alignment.CENTRE);

		// 设置标题
		sheet.mergeCells(0, 0, contents[0].length - 1, 0);
		Label titleLabel = new Label(0, 0, title, wcf_title);
		sheet.addCell(titleLabel);

		// 内容样式
		WritableFont wf = new WritableFont(WritableFont.TIMES, 10, WritableFont.NO_BOLD, false);
		WritableCellFormat wcf = new WritableCellFormat(wf);
		wcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);

		// 填充内容
		int[] columnWid = new int[contents[0].length];
		int contentLength = contents.length;
		if (contentLength > 60000) {
			contentLength = 60001;
		}

		for (int i = 0; i < contentLength; i++) {
			String[] cells = contents[i];// 行
			if (cells != null) {
				for (int j = 0; j < cells.length; j++) {
					String cell = cells[j];// 列
					if (cell == null) {
						cell = "";
					}

					// 设置列宽
					int cellLength = getWordCount(cell);
					if (columnWid[j] < cellLength) {
						columnWid[j] = cellLength;
						// 设置单元格格式 及 列宽
						CellView view = new CellView();
						view.setFormat(new WritableCellFormat(NumberFormats.TEXT));
						sheet.setColumnView(j, view);
						sheet.setColumnView(j, columnWid[j] + 1);
					}

					// 添加内容
					if (numFormat && j > 0 && cell.length() < 11 && NumberUtils.isNumber(cell)) {
						WritableCellFormat cellWcf = new WritableCellFormat(NumberFormats.THOUSANDS_FLOAT);
						cellWcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
						jxl.write.Number number = new jxl.write.Number(j, i + 1, NumberUtils.toDouble(cell), cellWcf);
						sheet.addCell(number);
					} else {
						sheet.addCell(new Label(j, i + 1, cell, wcf));
					}
				}
			}
		}

		// 输出数据
		wwb.write();
		wwb.close();
		os.close();
	}

	/**
	 * 读取excel为WorkBook
	 */
	public static Workbook readMultipartFile(MultipartFile file) {
		try {
			InputStream is = file.getInputStream();
			Workbook wb = Workbook.getWorkbook(is);
			is.close();
			return wb;
		} catch (Exception e) {
			LOGGER.error("读取excel异常:", e);
		}
		return null;
	}

	/*
	 * 由于Java是基于Unicode编码的，因此，一个汉字的长度为1，而不是2。
	 * 但有时需要以字节单位获得字符串的长度。例如，“123abc长城”按字节长度计算是10，而按Unicode计算长度是8。
	 * 为了获得10，需要从头扫描根据字符的Ascii来获得具体的长度
	 * 。如果是标准的字符，Ascii的范围是0至255，如果是汉字或其他全角字符，Ascii会大于255。
	 * 因此，可以编写如下的方法来获得以字节为单位的字符串长度。
	 */
	public static int getWordCount(String s) {
		int length = 0;
		for (int i = 0; i < s.length(); i++) {
			int ascii = Character.codePointAt(s, i);
			if (ascii >= 0 && ascii <= 255) {
				length++;
			} else {
				length += 2;
			}
		}
		return length;
	}

	/**
	 * 导出Excel
	 */
	public static void exportExcel2(String titlev, Object[][] contents, boolean numFormat, HttpServletResponse res)
			throws IOException, WriteException {
		OutputStream os = res.getOutputStream();
		res.setContentType("application/msexcel");
		// 标题样式
		WritableFont wf_title = new WritableFont(WritableFont.TIMES, 16, WritableFont.BOLD, false);
		WritableCellFormat wcf_title = new WritableCellFormat(wf_title);
		wcf_title.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
		wcf_title.setAlignment(Alignment.CENTRE);
		// 内容样式
		WritableFont wf = new WritableFont(WritableFont.TIMES, 10, WritableFont.NO_BOLD, false);
		WritableCellFormat wcf = new WritableCellFormat(wf);
		wcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
		// 创建Excel工作薄
		WritableWorkbook wwb = Workbook.createWorkbook(os);

		// 填充内容
		int[] columnWid = new int[contents[0].length];
		int contentLength = contents.length;
		int sheetnum = contentLength / 60000 + 1;
		int[] contentLengths = new int[sheetnum];
		for (int s = 0; s < sheetnum; s++) {
			String title = titlev;

			int sv = s > 0 ? 1 : 0;
			title = title + (sheetnum == 1 ? "" : "第" + (s + 1) + "部分");
			contentLengths[s] = contentLength - 60000 * (s + 1) < 0 ? contentLength : 60000 * (s + 1);
			// 添加第一个工作表并设置第一个Sheet的名字
			WritableSheet sheet = wwb.createSheet(title, s);
			// 设置标题
			sheet.mergeCells(0, 0, contents[0].length - 1, 0);
			Label titleLabel = new Label(0, 0, title.trim(), wcf_title);
			sheet.addCell(titleLabel);
			//附上标题
			if (s > 0) {
				Object[] cells1 = contents[0];// 行
				for (int j = 0; j < cells1.length; j++) {
					Object cell = cells1[j];// 列
					if (cell == null) {
						cell = "";
					}
					// 设置列宽
					int cellLength = getWordCount(cell.toString());
					if (columnWid[j] < cellLength) {
						columnWid[j] = cellLength;
						// 设置单元格格式 及 列宽
						CellView view = new CellView();
						view.setFormat(new WritableCellFormat(NumberFormats.TEXT));
						sheet.setColumnView(j, view);
						sheet.setColumnView(j, columnWid[j] + 1);
					}
					sheet.addCell(new Label(j, 1, cell.toString(), wcf));
				}
			}
			int v = 0;
			for (int i = s == 0 ? 0 : contentLengths[s - 1]; i < contentLengths[s]; i++) {
				Object[] cells = contents[i];// 行
				if (cells != null) {
					for (int j = 0; j < cells.length; j++) {
						Object cell = cells[j];// 列
						if (cell == null) {
							cell = "";
						}
						if (cell instanceof String) {
							String value = (String) cell;
							sheet.addCell(new Label(j, v + 1 + sv, value, wcf));
						} else if (cell instanceof Integer) {
							int value = (Integer) cell;
							// 添加内容
							if (numFormat && j > 0) {
								WritableCellFormat cellWcf = new WritableCellFormat(NumberFormats.THOUSANDS_INTEGER);
								cellWcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
								jxl.write.Number number = new jxl.write.Number(j, v + 1 + sv, value, cellWcf);
								sheet.addCell(number);
							} else {
								sheet.addCell(new Label(j, v + 1 + sv, value + "", wcf));
							}
						} else if (cell instanceof Double) {
							Double value = (Double) cell;
							// 添加内容
							if (numFormat && j > 0) {
								WritableCellFormat cellWcf = new WritableCellFormat(NumberFormats.THOUSANDS_FLOAT);
								cellWcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
								jxl.write.Number number = new jxl.write.Number(j, v + 1 + sv, value, cellWcf);
								sheet.addCell(number);
							} else {
								sheet.addCell(new Label(j, v + 1 + sv, value.toString(), wcf));
							}
						} else {
							Number value = (Number) cell;
							if (numFormat && j > 0) {
								WritableCellFormat cellWcf = new WritableCellFormat(NumberFormats.THOUSANDS_FLOAT);
								cellWcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
								jxl.write.Number number = new jxl.write.Number(j, v + 1 + sv, value.doubleValue(),
										cellWcf);
								sheet.addCell(number);
							} else {
								sheet.addCell(new Label(j, v + 1 + sv, value.toString(), wcf));
							}
						}

						// 设置列宽
						int cellLength = getWordCount(cell.toString());
						if (columnWid[j] < cellLength) {
							columnWid[j] = cellLength;
							// 设置单元格格式 及 列宽
							CellView view = new CellView();
							view.setFormat(new WritableCellFormat(NumberFormats.TEXT));
							sheet.setColumnView(j, view);
							sheet.setColumnView(j, columnWid[j] + 1);
						}

					}
				}
				v++;
			}
		}
		// 输出数据
		wwb.write();
		wwb.close();
		os.close();
	}

	public static void exportMultipleSheetExcel(String title, List<String[][]> contents, List<String> sheetTitles,
			boolean numFormat, HttpServletResponse res) throws IOException, WriteException {
		if (CollectionUtils.isEmpty(contents)) {
			return;
		}

		OutputStream os = res.getOutputStream();
		String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + title
				+ ".xls";
		res.setHeader("Content-Disposition",
				"attachment;filename=" + new String(fileName.getBytes("gb2312"), "ISO8859-1"));
		res.setContentType("application/msexcel");

		// 创建Excel工作薄
		WritableWorkbook wwb = Workbook.createWorkbook(os);

		try {
			for (int sheetIndex = 0; sheetIndex < contents.size(); sheetIndex++) {
				String[][] content = contents.get(sheetIndex);

				// 添加第一个工作表并设置第一个Sheet的名字
				String sheetTitle = title;
				if (CollectionUtils.isNotEmpty(sheetTitles) && sheetTitles.size() > sheetIndex) {
					sheetTitle = sheetTitles.get(sheetIndex);
				}
				WritableSheet sheet = wwb.createSheet(sheetTitle, sheetIndex);

				// 标题样式
				WritableFont wf_title = new WritableFont(WritableFont.TIMES, 16, WritableFont.BOLD, false);
				WritableCellFormat wcf_title = new WritableCellFormat(wf_title);
				wcf_title.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
				wcf_title.setAlignment(Alignment.CENTRE);

				// 设置标题
				sheet.mergeCells(0, 0, content[0].length - 1, 0);
				Label titleLabel = new Label(0, 0, title, wcf_title);
				sheet.addCell(titleLabel);

				// 内容样式
				WritableFont wf = new WritableFont(WritableFont.TIMES, 10, WritableFont.NO_BOLD, false);
				WritableCellFormat wcf = new WritableCellFormat(wf);
				wcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);

				// 填充内容
				int[] columnWid = new int[content[0].length];
				int contentLength = content.length;
				if (contentLength > 60000) {
					contentLength = 60001;
				}

				for (int i = 0; i < contentLength; i++) {
					String[] cells = content[i];// 行
					if (cells != null) {
						for (int j = 0; j < cells.length; j++) {
							String cell = cells[j];// 列
							if (cell == null) {
								cell = "";
							}

							// 设置列宽
							int cellLength = getWordCount(cell);
							if (columnWid[j] < cellLength) {
								columnWid[j] = cellLength;
								// 设置单元格格式 及 列宽
								CellView view = new CellView();
								view.setFormat(new WritableCellFormat(NumberFormats.TEXT));
								sheet.setColumnView(j, view);
								sheet.setColumnView(j, columnWid[j] + 1);
							}

							// 添加内容
							if (numFormat && j > 0 && cell.length() < 11 && NumberUtils.isNumber(cell)) {
								WritableCellFormat cellWcf = new WritableCellFormat(NumberFormats.THOUSANDS_FLOAT);
								cellWcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
								jxl.write.Number number = new jxl.write.Number(j, i + 1, NumberUtils.toDouble(cell),
										cellWcf);
								sheet.addCell(number);
							} else {
								sheet.addCell(new Label(j, i + 1, cell, wcf));
							}
						}
					}
				}
			}
			wwb.write();
		} finally {
			wwb.close();
			os.close();
		}
	}

}
