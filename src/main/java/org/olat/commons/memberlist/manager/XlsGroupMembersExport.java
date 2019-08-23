package org.olat.commons.memberlist.manager;

import org.apache.logging.log4j.Logger;
import org.olat.admin.landingpages.ui.RulesDataModel;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class XlsGroupMembersExport {

	private static final Logger log = Tracing.createLoggerFor(XlsGroupMembersExport.class);

	public MediaResource export(Map<String, GroupData> groups, Translator translator, List<UserPropertyHandler> userPropertyHandlers) {

		String label = "TableExport_"
				+ Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";

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
					createHeader(allowedPropertyHandlers, translator, sheet, workbook);
					// iterate through sorted key set (TreeSet is one of Set implementations that provides sorting):
					for (String groupName : new TreeSet<String>(groups.keySet())) {
						GroupData groupData = groups.get((String) groupName);
						createData(groupName, groupData.getMembers(), groupData.getRows(), allowedPropertyHandlers, sheet);
					}
				} catch (IOException e) {
					log.error("Unable to export xlsx", e);
				}
			}
		};
	}

	protected void createHeader(List<UserPropertyHandler> userPropertyHandlers, Translator translator,
							  OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
		OpenXMLWorksheet.Row headerRow = sheet.newRow();
		headerRow.addCell(0, translator.translate("form.name.group"), workbook.getStyles().getHeaderStyle());
		for (int c = 0; c < userPropertyHandlers.size(); c++) {
			UserPropertyHandler handler = userPropertyHandlers.get(c);
			String header = translator.translate("form.name." + handler.getName());
			headerRow.addCell(c + 1, header, workbook.getStyles().getHeaderStyle());
		}
		Translator roleTranslator = Util.createPackageTranslator(RulesDataModel.class, translator.getLocale());
		headerRow.addCell(userPropertyHandlers.size() + 1, roleTranslator.translate("rules.role"));
		sheet.setHeaderRows(1);
	}

	protected void createData(String groupName, Map<Identity, StringBuilder> members, List<Identity> rows,
							  List<UserPropertyHandler> userPropertyHandlers, OpenXMLWorksheet sheet) {
		for (int r = 0; r < rows.size(); r++) {
			OpenXMLWorksheet.Row dataRow = sheet.newRow();
			dataRow.addCell(0, groupName, null);
			for (int c = 0; c < userPropertyHandlers.size(); c++) {
				String value = userPropertyHandlers.get(c).getUserProperty(rows.get(r).getUser(), null);
				dataRow.addCell(c + 1, value, null);
			}
			dataRow.addCell(userPropertyHandlers.size() + 1, members.get(rows.get(r)).toString());
		}
	}

}
