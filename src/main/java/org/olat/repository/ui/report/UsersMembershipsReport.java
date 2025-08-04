/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.report;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.admin.user.UsermanagerUserSearchController;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.repository.RepositoryModule;
import org.olat.repository.manager.UsersMembershipsReportQuery;
import org.olat.repository.model.UsersMembershipsEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.admin.UserSearchTableController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UsersMembershipsReport extends OpenXMLWorkbookResource {
	
	private static final Logger log = Tracing.createLoggerFor(UsersMembershipsReport.class);
	
	public static final String USER_PROPS_IDENTIFIER = UsersMembershipsReport.class.getCanonicalName();
	private static final int BATCH_SIZE = 10000;
	
	private final Date from;
	private final Date to;
	private final List<GroupRoles> roles;
	private final Translator translator;
	private List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private UsersMembershipsReportQuery reportQuery;
	
	public UsersMembershipsReport(String label, Date from, Date to, List<GroupRoles> roles, Translator translator) {
		super(label);
		this.from = from;
		this.to = to;
		this.roles = roles;
		this.translator = userManager.getPropertyHandlerTranslator(Util
				.createPackageTranslator(UsermanagerUserSearchController.class, translator.getLocale(), Util
						.createPackageTranslator(UserSearchTableController.class, translator.getLocale(), translator)));

		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_IDENTIFIER, true);
		
	}

	@Override
	protected void generate(OutputStream out) {
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			addHeaders(exportSheet);
			addContent(exportSheet, workbook);
		} catch (IOException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void addHeaders(OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);
		
		Row headerRow = exportSheet.newRow();
		
		int pos = 0;
		headerRow.addCell(pos++, translator.translate("table.header.key"));
		headerRow.addCell(pos++, translator.translate("table.header.externalref"));
		if(repositoryModule.isManagedRepositoryEntries()) {
			headerRow.addCell(pos++, translator.translate("table.header.externalid"));
		}
		headerRow.addCell(pos++, translator.translate("table.header.displayname"));
		headerRow.addCell(pos++, translator.translate("table.header.lifecycle.start"));
		headerRow.addCell(pos++, translator.translate("table.header.lifecycle.end"));
		headerRow.addCell(pos++, translator.translate("table.header.access"));
		headerRow.addCell(pos++, translator.translate("table.header.created.by"));
		headerRow.addCell(pos++, translator.translate("report.share.header"));
		headerRow.addCell(pos++, translator.translate("table.header.taxonomy.levels"));
		
		// Membership
		headerRow.addCell(pos++, translator.translate("table.header.membership.role"));
		headerRow.addCell(pos++, translator.translate("table.header.membership.date"));
		
		// Identity
		headerRow.addCell(pos++, translator.translate("table.identity.id"));
		for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
			headerRow.addCell(pos++, translator.translate(userPropertyHandler.i18nFormElementLabelKey()));
		}
		headerRow.addCell(pos++, translator.translate("table.identity.organisations"));
		headerRow.addCell(pos++, translator.translate("table.identity.status"));
		headerRow.addCell(pos++, translator.translate("table.identity.creationdate"));
		headerRow.addCell(pos++, translator.translate("table.identity.lastlogin"));
	}
	

	private void addContent(UsersMembershipsEntry entry, OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		Row row = exportSheet.newRow();
		
		int pos = 0;
		// Course
		row.addCell(pos++, entry.getRepositoryEntryKey(), workbook.getStyles().getIntegerStyle());
		row.addCell(pos++, entry.getRepositoryEntryExternalRef());
		if(repositoryModule.isManagedRepositoryEntries()) {
			row.addCell(pos++, entry.getRepositoryEntryExternalId());
		}
		row.addCell(pos++, entry.getRepositoryEntryDisplayname());
		row.addCell(pos++, entry.getLifecycleFrom(), workbook.getStyles().getDateStyle());
		row.addCell(pos++, entry.getLifecycleTo(), workbook.getStyles().getDateStyle());
		row.addCell(pos++, translator.translate("table.status." + entry.getRepositoryEntryStatus()));
		row.addCell(pos++, translator.translate("table.initial.author", entry.getRepositoryEntryInitialAuthorName(), 
				entry.getRepositoryEntryInitialAuthorEmail()));
		row.addCell(pos++, toPrivatePublic(entry));
		row.addCell(pos++, listToString(entry.getTaxonomyLevels()));
		
		// Membership
		row.addCell(pos++, translator.translate("role." + entry.getRole().name()));
		row.addCell(pos++, entry.getRegistrationDate(), workbook.getStyles().getDateStyle());
		
		// Identity
		row.addCell(pos++, entry.getIdentityKey(), workbook.getStyles().getIntegerStyle());
		for(int i=0; i<userPropertyHandlers.size(); i++) {
			row.addCell(pos++, entry.getIdentityProp(i));
		}
		row.addCell(pos++, listToString(entry.getOrganisations()));
		row.addCell(pos++, toIdentityStatus(entry));
		row.addCell(pos++, entry.getIdentityCreationDate(), workbook.getStyles().getDateStyle());
		row.addCell(pos++, entry.getIdentityLastLogin(), workbook.getStyles().getDateStyle());
	}
	
	private String toPrivatePublic(UsersMembershipsEntry entry) {
		Boolean publicVisible = entry.getRepositoryEntryPublicVisible();
		if(publicVisible == null) {
			return "";
		}
		return publicVisible.booleanValue()
				? translator.translate("report.bookable")
				: translator.translate("cif.access.membersonly.short");
	}
	
	private String listToString(List<String> list) {
		if(list == null || list.isEmpty()) {
			return "";
		}
		if(list.size() == 1) {
			return list.get(0);
		}
		return String.join(", ", list);
	}
	
	private String toIdentityStatus(UsersMembershipsEntry entry) {
		int cellValue = entry.getIdentityStatus();
		String i18nKey = null;
		if(Identity.STATUS_ACTIV.intValue() == cellValue) {
			i18nKey = "rightsForm.status.activ";
		} else if(Identity.STATUS_PERMANENT.intValue() == cellValue) {
			i18nKey = "rightsForm.status.permanent";
		} else if(Identity.STATUS_LOGIN_DENIED.intValue() == cellValue) {
			i18nKey = "rightsForm.status.login_denied";
		} else if(Identity.STATUS_PENDING.intValue() == cellValue) {
			i18nKey = "rightsForm.status.pending";
		} else if(Identity.STATUS_INACTIVE.intValue() == cellValue) {
			i18nKey = "rightsForm.status.inactive";
		} else if(Identity.STATUS_DELETED.intValue() == cellValue) {
			i18nKey = "rightsForm.status.deleted";
		}
		return i18nKey == null ? "" : translator.translate(i18nKey);
	}

	private void addContent(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		int firstResult = 0;
		List<UsersMembershipsEntry> entries;
		do {
			entries = reportQuery.search(from, to, roles, userPropertyHandlers, translator.getLocale(), firstResult, BATCH_SIZE);
			firstResult += entries.size();
			for(UsersMembershipsEntry entry:entries) {
				addContent(entry, exportSheet, workbook);
			}
		} while(entries.size() == BATCH_SIZE);	
	}
}
