package com.springMVC.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.springMVC.dao.VesselDao;
import com.springMVC.entity.Vessel;

@Service
public class ImportHandler {

	private static final Log LOG = LogFactory.getLog(ImportHandler.class);

	@Resource
	private VesselDao vesselDao;
	
	@Resource 
	private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		try {
			// ApplicationContext ctx = new
			// ClassPathXmlApplicationContext("springMVC-servlet.xml");
			// VesselDao vesselDao = (VesselDao)ctx.getBean("vesselDaoImpl");
			// ImportHandler ih=new ImportHandler();
			File returnFile = new File("D\\upload", "1234.excel");
			LOG.debug(returnFile.getPath());
			// ih.importVessel("D:\\123.xls");//ImportVessel

			System.out.println((57 % 2 != 0)
					+ " ImportHandlerImportHandlerImportHandler(): "
					+ returnFile.getPath());

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(" getMessage(): " + e.getMessage());
		}
	}

	public File uploadFile(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = PropertiesUtil.getPropertiesValue("uploadFolder");
		File returnFile = null;
		// file upload factory
		DiskFileItemFactory factory = new DiskFileItemFactory();

		// setting upload file path
		factory.setRepository(new File(path));

		// set default memory size
		factory.setSizeThreshold(1024 * 1024 * 1);

		//
		ServletFileUpload upload = new ServletFileUpload(factory);

		try {
			List<FileItem> list = upload.parseRequest(request);
			for (FileItem item : list) {
				if (item.isFormField()) { // 过滤掉表单中非文件域
					String name = item.getFieldName();// input name
					String value = item.getName();// input content
					request.setAttribute(name, value);
				} else {
					String name = item.getFieldName();// input name
					String value = item.getName();// input content
					value = value.substring(value.lastIndexOf("\\") + 1, value.length());

					// output file
					returnFile = new File(path, value);
					LOG.debug(returnFile.getPath());
					OutputStream fileOutStream = new FileOutputStream(returnFile);
					// input file
					InputStream fileInputStream = item.getInputStream();

					// file buffer
					byte[] buffer = new byte[1024];

					// read
					int length = 0;

					while ((length = fileInputStream.read(buffer)) > 0) {
						fileOutStream.write(buffer, 0, length);
					}

					// close
					fileInputStream.close();
					fileOutStream.close();
					item.write(new File(path, value));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.debug(e.toString());
		}

		return returnFile;
	}

	public void importVessel(File file) throws GeneralException {
		// File file = new File(filename);
		try {
			String[][] result = null; 
					
			String fileName = file.getName();
			String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			if ("xls".equals(fileExt) || "xlsx".equals(fileExt)) {
				result = getExcelData(file, 1);
			} else if ("txt".equals(fileExt)) {
				result = getTxtData(file);
			}

			int rowLength = result.length;

			for (int i = 0; i < rowLength; i++) {
				Vessel vessel = new Vessel();
				for (int j = 0; j < result[i].length; j++) {

					LOG.debug(result[i][j] + "\t\t" + i + " : " + j);
					if (j == 0) {
						vessel.setVesselid(result[i][j]);
					} else if (j == 1) {
						vessel.setDeck_hold(result[i][j]);
					} else if (j == 2) {
						vessel.setBay(result[i][j]);
					} else if (j == 3) {
						vessel.setRowStart(result[i][j]);
					} else if (j == 4) {
						vessel.setRowEnd(result[i][j]);
					} else if (j == 5) {
						vessel.setTierStart(result[i][j]);
					} else if (j == 6) {
						vessel.setTierEnd(result[i][j]);
					}

				}
				vesselDao.save(vessel);
				// vessel.toString();
			}

		} catch (Exception e) {
			e.printStackTrace();
			LOG.debug(e.toString());
			if ("error_no_vessel_found_in_n4".equals(e.getMessage())) {
				throw new GeneralException("error_no_vessel_found_in_n4");
			} else {
				throw new GeneralException("error_query_db_error");
			}
		}
	}

	private String[][] getTxtData(File file) throws FileNotFoundException, IOException, GeneralException {
		String fileContent = getFileContent(file);
		String vesselId = "";
		String[] bayPlanHeader = null;
		String[] bayPlanRows = null;
		String[] customBayPlanHeader = null;
		String[] customBayPlanRows = null;
		
		Pattern p = Pattern.compile("\\*SHIP\\r\\n.*\\r\\n(.*?)\\t");
		Matcher m = p.matcher(fileContent);
		
		if (m.find()) {
			vesselId = m.group(1).trim();
		}
		
		p = Pattern.compile("\\*STACK\\r\\n\\*\\*(.*)\\r\\n([\\s\\S]*?)\\*");
		m = p.matcher(fileContent);
		
		if (m.find()) {
			bayPlanHeader = m.group(1).split("\t");
			bayPlanRows = m.group(2).split("\r\n");
		}
		
		p = Pattern.compile("\\*TIER\\r\\n\\*\\*(.*)\\r\\n([\\s\\S]*?)\\*");
		m = p.matcher(fileContent);
		
		if (m.find()) {
			customBayPlanHeader = m.group(1).split("\t");
			customBayPlanRows = m.group(2).split("\r\n");
		}
		
		
		
		int stafBayIndex = -1;
		int levelIndex = -1;
		int isoStackIndex = -1;
		int topTierIndex = -1;
		int bottomTierIndex = -1;
		
		for (int i = 0; i < bayPlanHeader.length; ++i) {
			String header = bayPlanHeader[i].trim().toUpperCase();
			
			switch (header) {
				case "STAF BAY":
					stafBayIndex = i;
					break;
				case "LEVEL":
					levelIndex = i;
					break;
				case "ISO STACK":
					isoStackIndex = i;
					break;
				case "TOP TIER":
					topTierIndex = i;
					break;
				case "BOTTOM TIER":
					bottomTierIndex = i;
					break;
			}
		}
		
		if (stafBayIndex == -1 || levelIndex == -1 || isoStackIndex == -1 || topTierIndex == -1 || bottomTierIndex == -1) {
			throw new GeneralException("import_vessel_file_empty");
		}
		
		Map<String, String[]> bayLevels = new HashMap<String, String[]>();
		final int VESSEL = 0;
		final int LEVEL = 1;
		final int BAY = 2;
		final int ROW_START = 3;
		final int ROW_END = 4;
		final int TIER_START = 5;
		final int TIER_END = 6;
		
		for (String bayPlanRow : bayPlanRows) {
			if ("".equals(bayPlanRow.replaceAll("\\s", ""))) {
				continue;
			}
			
			String[] bayPlanCells = bayPlanRow.split("\t");
			String stafBay = bayPlanCells[stafBayIndex].replaceAll("\\s", "");
			String level = bayPlanCells[levelIndex].replaceAll("\\s", "");
			String isoStack = bayPlanCells[isoStackIndex].replaceAll("\\s", "");
			String topTier = bayPlanCells[topTierIndex].replaceAll("\\s", "");
			String bottomTier = bayPlanCells[bottomTierIndex].replaceAll("\\s", "");
			
			String bayLevel = stafBay + level;
			String[] bayPlan = new String[7];
			
			if (!bayLevels.containsKey(bayLevel)) {
				bayPlan[BAY] = stafBay;
				bayPlan[LEVEL] = level;
				bayPlan[ROW_START] = isoStack;
				bayPlan[ROW_END] = isoStack;
				bayPlan[TIER_START] = bottomTier;
				bayPlan[TIER_END] = topTier;
				
				bayLevels.put(bayLevel, bayPlan);
			} else {
				bayPlan = bayLevels.get(bayLevel);
				if (Integer.parseInt(isoStack) < Integer.parseInt(bayPlan[ROW_START])) {
					bayPlan[ROW_START] = isoStack;
				}
				if (Integer.parseInt(isoStack) > Integer.parseInt(bayPlan[ROW_END])) {
					bayPlan[ROW_END] = isoStack;
				}
				if (Integer.parseInt(bottomTier) < Integer.parseInt(bayPlan[TIER_START])) {
					bayPlan[TIER_START] = bottomTier;
				}
				if (Integer.parseInt(topTier) > Integer.parseInt(bayPlan[TIER_END])) {
					bayPlan[TIER_END] = topTier;
				}
			}
		}
		
		// CR: Override tier number for custom bay
		if (customBayPlanHeader != null) {
			int customStafBayIndex = -1;
			int customLevelIndex = -1;
			int customTierIndex = -1;
			
			for (int i = 0; i < customBayPlanHeader.length; ++i) {
				String header = customBayPlanHeader[i].trim().toUpperCase();
				
				switch (header) {
					case "STAF BAY":
						customStafBayIndex = i;
						break;
					case "LEVEL":
						customLevelIndex = i;
						break;
					case "CUSTOM TIER":
						customTierIndex = i;
						break;
				}
			}
			
			Map<String, String[]> customBayLevels = new HashMap<String, String[]>();
			
			final int CUSTOM_BAY = 0;
			final int CUSTOM_LEVEL = 1;
			final int CUSTOM_TIER_START = 2;
			final int CUSTOM_TIER_END = 3;
			
			for (String customBayPlanRow : customBayPlanRows) {
				if ("".equals(customBayPlanRow.replaceAll("\\s", ""))) {
					continue;
				}
				
				String[] customBayPlanCells = customBayPlanRow.split("\t");
				String customStafBay = customBayPlanCells[customStafBayIndex].replaceAll("\\s", "");
				String customLevel = customBayPlanCells[customLevelIndex].replaceAll("\\s", "");
				String customTier = customBayPlanCells[customTierIndex].replaceAll("[\\s\\-]", "");
				
				String customBayLevel = customStafBay + customLevel;
				String[] customBayPlan = new String[4];
				
				if (customTier.isEmpty()) {
					continue;
				}
				
				if (!customBayLevels.containsKey(customBayLevel)) {
					customBayPlan[CUSTOM_BAY] = customStafBay;
					customBayPlan[CUSTOM_LEVEL] = customLevel;
					customBayPlan[CUSTOM_TIER_START] = customTier;
					customBayPlan[CUSTOM_TIER_END] = customTier;
					
					customBayLevels.put(customBayLevel, customBayPlan);
				} else {
					customBayPlan = customBayLevels.get(customBayLevel);
					if (Integer.parseInt(customTier) < Integer.parseInt(customBayPlan[CUSTOM_TIER_START])) {
						customBayPlan[CUSTOM_TIER_START] = customTier;
					}
					if (Integer.parseInt(customTier) > Integer.parseInt(customBayPlan[CUSTOM_TIER_END])) {
						customBayPlan[CUSTOM_TIER_END] = customTier;
					}
				}
			}
			
			// Merge into original bay plan array
			if (customBayLevels.size() > 0) {
				Set<String> bl = bayLevels.keySet();
				for (String k : bl) {
					if (!customBayLevels.containsKey(k)) {
						continue;
					}
					
					String[] bayPlan = bayLevels.get(k);
					String[] customBayPlan = customBayLevels.get(k);
					bayPlan[TIER_START] = customBayPlan[CUSTOM_TIER_START];
					bayPlan[TIER_END] = customBayPlan[CUSTOM_TIER_END];
				}
			}
		}
		
		String[][] data = new String[bayLevels.size()][7];
		
		String vesselName = "";
		
		try {
			vesselName = vesselDao.getN4VesselNameById(vesselId);
		} catch (Exception e) {
			throw new GeneralException("error_no_vessel_found_in_n4");
		}
		
		int row = 0;
		for (Entry<String, String[]> bayLevel : bayLevels.entrySet()) {
			String[] bayPlan = bayLevel.getValue();
			
			bayPlan[VESSEL] = vesselName;
			bayPlan[BAY] = Integer.parseInt(bayPlan[BAY]) + "";
			bayPlan[ROW_START] = Integer.parseInt(bayPlan[ROW_START]) + "";
			bayPlan[ROW_END] = Integer.parseInt(bayPlan[ROW_END]) + "";
			bayPlan[TIER_START] = Integer.parseInt(bayPlan[TIER_START]) + "";
			bayPlan[TIER_END] = Integer.parseInt(bayPlan[TIER_END]) + "";
			
			data[row++] = bayPlan;
		}
		
		return data;
	}
	
	private String getFileContent(File file) throws FileNotFoundException, IOException {
		BufferedReader bf = new BufferedReader(new FileReader(file));
		String content = "";
		StringBuilder sb = new StringBuilder();
		while (content != null) {
			content = bf.readLine();
			
			if (content == null) {
				break;
			}
			
			sb.append(content + "\r\n");
		}
		
		bf.close();
		
		return sb.toString();
	}

	public String[][] getExcelData(File file, int ignoreRows) throws GeneralException {
		List<String[]> result = new ArrayList<String[]>();

		int rowSize = 0;
		
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

			// 打开HSSFWorkbook
			POIFSFileSystem fs = new POIFSFileSystem(in);
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFCell cell = null;

			for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
				HSSFSheet st = wb.getSheetAt(sheetIndex);

				// 第一行为标题，不取
				for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
					HSSFRow row = st.getRow(rowIndex);
					if (row == null) {
						continue;
					}

					int tempRowSize = row.getLastCellNum() + 1;
					if (tempRowSize > rowSize) {
						rowSize = tempRowSize;
					}

					String[] values = new String[rowSize];
					Arrays.fill(values, "");
					boolean hasValue = false;

					for (short columnIndex = 0; columnIndex <= row.getLastCellNum(); columnIndex++) {
						String value = "";
						cell = row.getCell(columnIndex);

						if (cell != null) {
							// 注意：一定要设成这个，否则可能会出现乱码
							// cell.setEncoding(HSSFCell.ENCODING_UTF_16);

							switch (cell.getCellType()) {
							case HSSFCell.CELL_TYPE_STRING:
								value = cell.getStringCellValue();
								break;
							case HSSFCell.CELL_TYPE_NUMERIC:
								value = new DecimalFormat("0").format(cell.getNumericCellValue());
								break;
							case HSSFCell.CELL_TYPE_FORMULA:
								// 导入时如果为公式生成的数据则无值
								if (!cell.getStringCellValue().equals("")) {
									value = cell.getStringCellValue();
								} else {
									value = cell.getNumericCellValue() + "";
								}
								break;
							case HSSFCell.CELL_TYPE_BLANK:
								break;
							case HSSFCell.CELL_TYPE_ERROR:
								value = "";
								break;
							case HSSFCell.CELL_TYPE_BOOLEAN:
								value = (cell.getBooleanCellValue() == true ? "Y" : "N");
								break;
							default:
								value = "";
							}
						}

						if (columnIndex == 0 && value.trim().equals("")) {
							break;
						}

						values[columnIndex] = rightTrim(value);

						hasValue = true;
					}

					if (hasValue) {
						result.add(values);
					}
				}
			}

			in.close();

			String[][] returnArray = new String[result.size()][rowSize];

			for (int i = 0; i < returnArray.length; i++) {
				returnArray[i] = (String[]) result.get(i);
			}

			return returnArray;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.debug(e.toString());
			throw new GeneralException("error_query_db_error");
		}
	}

	/**
	 * 
	 * 去掉字符串右边的空格
	 * 
	 * @param str
	 *            要处理的字符串
	 * 
	 * @return 处理后的字符串
	 */

	public static String rightTrim(String str) {
		if (str == null) {
			return "";
		}

		int length = str.length();

		for (int i = length - 1; i >= 0; i--) {
			if (str.charAt(i) != 0x20) {
				break;
			}
			length--;
		}

		return str.substring(0, length);
	}
}
