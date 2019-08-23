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
package org.olat.commons.memberlist.manager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.admin.landingpages.ui.RulesDataModel;
import org.olat.commons.memberlist.model.CurriculumElementInfos;
import org.olat.commons.memberlist.model.CurriculumMemberInfos;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial Date: 23.03.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class XlsMembersExport {
	
	private static final Logger log = Tracing.createLoggerFor(XlsMembersExport.class);


	public MediaResource export(List<Identity> rows, Map<Identity, StringBuilder> members, Map<Long,CurriculumMemberInfos> curriculumInfos,
			Translator translator, List<UserPropertyHandler> userPropertyHandlers) {
	
		String label = "TableExport_"
				+ Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
		
		boolean curriculum = curriculumInfos != null && !curriculumInfos.isEmpty();
		
		return new OpenXMLWorkbookResource(label) {
			@Override
			protected void generate(OutputStream out) {
				try (OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
					OpenXMLWorksheet sheet = workbook.nextWorksheet();
					// LMSUZH-566: Do not export email addresses even if email functionality is enabled in course element
					List<UserPropertyHandler> allowedPropertyHandlers = userPropertyHandlers
							.stream()
							.filter(userPropertyHandler -> !UserConstants.EMAIL.equals(userPropertyHandler.getName()))
							.collect(Collectors.toList());
					createHeader(userPropertyHandlers, curriculum, translator, sheet, workbook);
					createData(members, rows, userPropertyHandlers, curriculumInfos, sheet);
				} catch (IOException e) {
					log.error("Unable to export xlsx", e);
				}
			}
		};
	}

	private void createHeader(List<UserPropertyHandler> userPropertyHandlers, boolean curriculum, Translator translator, 
			OpenXMLWorksheet sheet,	OpenXMLWorkbook workbook) {
		Row headerRow = sheet.newRow();
		int c = 0;
		for ( ; c < userPropertyHandlers.size(); c++) {
			UserPropertyHandler handler = userPropertyHandlers.get(c);
			String header = translator.translate("form.name." + handler.getName());
			headerRow.addCell(c, header, workbook.getStyles().getHeaderStyle());
		}
		if(curriculum) {
			headerRow.addCell(c++, translator.translate("table.header.curriculum"), workbook.getStyles().getHeaderStyle());
			headerRow.addCell(c++, translator.translate("table.header.curriculum.root.identifier"), workbook.getStyles().getHeaderStyle());
		}
		
		Translator roleTranslator = Util.createPackageTranslator(RulesDataModel.class, translator.getLocale());
		headerRow.addCell(c++, roleTranslator.translate("rules.role"));
	}

	private void createData(Map<Identity, StringBuilder> members, List<Identity> rows, List<UserPropertyHandler> userPropertyHandlers,
			Map<Long,CurriculumMemberInfos> curriculumInfos, OpenXMLWorksheet sheet) {
		sheet.setHeaderRows(1);
		for (int r = 0; r < rows.size(); r++) {
			Row dataRow = sheet.newRow();
			int c = 0;
			Identity identity = rows.get(r);
			for ( ; c < userPropertyHandlers.size(); c++) {
				String value = userPropertyHandlers.get(c).getUserProperty(identity.getUser(), null);
				dataRow.addCell(c, value, null);
			}
			
			if(curriculumInfos != null && !curriculumInfos.isEmpty()) {
				CurriculumMemberInfos curriculumMemberInfos = curriculumInfos.get(identity.getKey());
				if(curriculumMemberInfos != null && !curriculumMemberInfos.getCurriculumInfos().isEmpty()) {
					CurriculumElementInfos infos = curriculumMemberInfos.getCurriculumInfos().get(0);
					dataRow.addCell(c++, infos.getCurriculumDisplayName(), null);
					dataRow.addCell(c++, infos.getRootElementIdentifier(), null);
				} else {
					c = c + 2;
				}
			}
			
			dataRow.addCell(c++, members.get(rows.get(r)).toString());
		}
	}

}
