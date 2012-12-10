/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.instantMessaging.manager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.pdfbox.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.WorkbookMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.instantMessaging.InstantMessage;
import org.olat.instantMessaging.model.InstantMessageImpl;
import org.olat.instantMessaging.ui.ChatController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 10.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ChatLogHelper {
	private static final OLog log = Tracing.createLoggerFor(ChatLogHelper.class);
	
	private XStream logXStream;
	private static final int BATCH_SIZE = 100;

	@Autowired
	private InstantMessageDAO imDao;
	
	@PostConstruct 
	public void init() {
		logXStream = XStreamHelper.createXStreamInstance();
		logXStream.alias("message", InstantMessageImpl.class);
		logXStream.alias("identity", IdentityImpl.class);
		logXStream.omitField(IdentityImpl.class, "user");
	}
	
	public void archive(OLATResourceable ores, File exportDirectory) {
		ObjectOutputStream out = null;
		try {
			File file = new File(exportDirectory, "chat.xml");
			Writer writer = new FileWriter(file);
			out = logXStream.createObjectOutputStream(writer);
			
			int counter = 0;
			List<InstantMessage> messages;
			do {
				messages = imDao.getMessages(ores, counter, BATCH_SIZE);
				for(InstantMessage message:messages) {
					out.writeObject(message);
				}
				counter += messages.size();
			} while(messages.size() == BATCH_SIZE);
		} catch (IOException e) {
			log.error("", e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}
	
	public MediaResource logMediaResource(OLATResourceable ores, Locale locale) {
		Workbook wb = log(ores, locale);
		WorkbookMediaResource resource = new WorkbookMediaResource(wb);
		return resource;		
	}
	
	public Workbook log(OLATResourceable ores, Locale locale) {
		Translator translator = Util.createPackageTranslator(ChatController.class, locale);

		Workbook wb = new HSSFWorkbook();
		String tableExportTitle = translator.translate("logChat.export.title");
		Sheet exportSheet = wb.createSheet(tableExportTitle);
		
		//headers
		Row headerRow = exportSheet.createRow(0);
		CellStyle headerCellStyle = getHeaderCellStyle(wb);
		addHeader(headerRow, headerCellStyle, "User", 0);
		addHeader(headerRow, headerCellStyle, "Date", 1);
		addHeader(headerRow, headerCellStyle, "Content", 2);
		
		//content
		List<InstantMessage> messages = imDao.getMessages(ores, 0, -1);
		int count = 1;
		for(InstantMessage message:messages) {
			Row dataRow = exportSheet.createRow(count);
			addCell(dataRow, message.getFromNickName(), 0);
			addCell(dataRow, message.getCreationDate(), 1);
			addCell(dataRow, message.getBody(), 2);
		}
		return wb;
	}

	private void addCell(Row dataRow, String val, int position) {
		val = FilterFactory.getHtmlTagsFilter().filter(val);
		Cell cell = dataRow.createCell(position);
		cell.setCellValue(val);
	}
	
	private void addCell(Row dataRow, Date val, int position) {
		Cell cell = dataRow.createCell(position);
		cell.setCellValue(val);
	}
	
	private void addHeader(Row headerRow, CellStyle headerCellStyle, String val, int position) {
		Cell cell = headerRow.createCell(position);
		cell.setCellValue(val);
		cell.setCellStyle(headerCellStyle);
	}
	
	private CellStyle getHeaderCellStyle(final Workbook wb) {
		CellStyle cellStyle = wb.createCellStyle();
		Font boldFont = wb.createFont();
		boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		cellStyle.setFont(boldFont);
		return cellStyle;
	}


}
