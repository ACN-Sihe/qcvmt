package com.springMVC.util;

import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

import com.springMVC.entity.ShowLog;

@Service
public class ExportHandler {

	
	public void exportQCLog(HttpServletResponse response, List exportLogsList) throws ParseException {
		HSSFWorkbook wb = new HSSFWorkbook();
		
		HSSFSheet sheet = wb.createSheet("Logs List");
		
		HSSFRow row = sheet.createRow((int) 0);
		
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		HSSFCell cell = row.createCell((short) 0);
		cell.setCellValue("用户名");
		cell.setCellStyle(style);
		cell = row.createCell((short) 1);
		cell.setCellValue("QC号码");
		cell.setCellStyle(style);
		cell = row.createCell((short) 2);
		cell.setCellValue("操作");
		cell.setCellStyle(style);
		cell = row.createCell((short) 3);
		cell.setCellValue("时间");
		cell.setCellStyle(style);
		cell = row.createCell((short) 4);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
		if(exportLogsList != null && exportLogsList.size()>0){
			for (int i = 0; i < exportLogsList.size(); i++)
			{
				row = sheet.createRow((int) i + 1);
				ShowLog log = (ShowLog) exportLogsList.get(i);
				row.createCell((short) 0).setCellValue(log.getUsername());
				row.createCell((short) 1).setCellValue(log.getQcid());
				row.createCell((short) 2).setCellValue(log.getOperation());
				row.createCell((short) 3).setCellValue(sdf2.format(sdf.parse(log.getLoginTime())));
				cell = row.createCell((short) 4);
			//	cell.setCellValue(new SimpleDateFormat("yyyy-mm-dd").format(new Date()));
			}
		}
		
		try
		{
			response.reset();
			response.setContentType("bin");
			response.setHeader("Content-disposition","attachment;filename="+WebUtil.getDateTimeNow()+".xls");
			OutputStream os = response.getOutputStream();
			wb.write(os);
			os.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
