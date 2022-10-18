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
import java.io.OutputStream;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
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
	private static final Logger log = Tracing.createLoggerFor(ChatLogHelper.class);
	
	private XStream logXStream;
	private static final int BATCH_SIZE = 100;

	@Autowired
	private InstantMessageDAO imDao;
	
	@PostConstruct 
	public void init() {
		logXStream = XStreamHelper.createXStreamInstance();
		XStreamHelper.allowDefaultPackage(logXStream);
		logXStream.alias("message", InstantMessageImpl.class);
		logXStream.alias("identity", IdentityImpl.class);
		logXStream.omitField(IdentityImpl.class, "user");
	}
	
	public void archiveResource(OLATResourceable ores, File exportDirectory) {
		File file = new File(exportDirectory, "chat.xml");
		try(Writer writer = new FileWriter(file);
			ObjectOutputStream out = logXStream.createObjectOutputStream(writer)) {		
			int counter = 0;
			List<InstantMessage> messages;
			do {
				messages = imDao.getMessages(ores, null, null, null, counter, BATCH_SIZE);
				for(InstantMessage message:messages) {
					out.writeObject(message);
				}
				counter += messages.size();
			} while(messages.size() == BATCH_SIZE);
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	public MediaResource logMediaResource(final OLATResourceable ores, final String resSubPath, final String channel, Locale locale) {
		Translator translator = Util.createPackageTranslator(ChatController.class, locale);
		String tableExportTitle = translator.translate("logChat.export.title");
		String label = tableExportTitle
				+ Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
		
		return new OpenXMLWorkbookResource(label) {
			@Override
			protected void generate(OutputStream out) {
				try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
					//headers
					OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
					Row headerRow = exportSheet.newRow();
					headerRow.addCell(0, translator.translate("log.user"), workbook.getStyles().getHeaderStyle());
					headerRow.addCell(1, translator.translate("log.date"), workbook.getStyles().getHeaderStyle());
					headerRow.addCell(2, translator.translate("log.content"), workbook.getStyles().getHeaderStyle());

					//content
					List<InstantMessage> messages = imDao.getMessages(ores, resSubPath, channel, null, 0, -1);
					for(InstantMessage message:messages) {
						
						Row dataRow = exportSheet.newRow();
						dataRow.addCell(0, message.getFromNickName(), null);
						dataRow.addCell(1, message.getCreationDate(), workbook.getStyles().getDateStyle());
						if(message.getType().isStatus()) {
							dataRow.addCell(2, translator.translate("log.status." + message.getType().name()), null);
						} else {
							dataRow.addCell(2, message.getBody(), null);
						}
					}
				} catch (IOException e) {
					log.error("", e);
				}
			}
		};		
	}
}
