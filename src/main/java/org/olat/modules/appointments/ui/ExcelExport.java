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
package org.olat.modules.appointments.ui;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Appointment.Status;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Participation;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExcelExport {
	
	private static final Logger log = Tracing.createLoggerFor(ExcelExport.class);
	
	private static final String usageIdentifyer = ExcelExport.class.getCanonicalName();
	
	private final ParticipationSearchParams searchParams;
	private final String fileName;
	private final Translator translator;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserManager userManager;

	public ExcelExport(UserRequest ureq, ParticipationSearchParams searchParams, String fileName) {
		this.fileName = StringHelper.transformDisplayNameToFileSystemName(fileName) + ".xlsx";
		this.searchParams = searchParams;
		CoreSpringFactory.autowireObject(this);
		
		translator = userManager.getPropertyHandlerTranslator(
				Util.createPackageTranslator(AppointmentsMainController.class, ureq.getLocale()));
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
	}
	
	public OpenXMLWorkbookResource createMediaResource() {
		return new OpenXMLWorkbookResource(fileName) {
			@Override
			protected void generate(OutputStream out) {
				createWorkbook(out);
			}
		};
	}
	
	private void createWorkbook(OutputStream out) {
		List<String> sheetNames = Collections.singletonList(translator.translate("participants"));
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1, sheetNames)) {
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			addHeader(exportSheet);
			addContent(workbook, exportSheet);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void addHeader(OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);
		
		int col = 0;
		Row headerRow = exportSheet.newRow();
		headerRow.addCell(col++, translator.translate("appointment.start"));
		headerRow.addCell(col++, translator.translate("appointment.end"));
		headerRow.addCell(col++, translator.translate("appointment.location"));
		headerRow.addCell(col++, translator.translate("appointment.status"));
		
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			headerRow.addCell(col + i, translator.translate(userPropertyHandler.i18nColumnDescriptorLabelKey()));
		}
	}
	
	private void addContent(OpenXMLWorkbook workbook, OpenXMLWorksheet worksheet) {
		searchParams.setFetchAppointments(true);
		searchParams.setFetchIdentities(true);
		searchParams.setFetchUser(true);
		
		appointmentsService.getParticipations(searchParams).stream()
				.sorted((p1, p2) -> p1.getAppointment().getStart().compareTo(p2.getAppointment().getStart()))
				.forEach(participation -> addContent(workbook, worksheet, participation));
	}

	private void addContent(OpenXMLWorkbook workbook, OpenXMLWorksheet worksheet, Participation participation) {
		Row dataRow = worksheet.newRow();
		
		int col = 0;
		Appointment appointment = participation.getAppointment();
		dataRow.addCell(col++, appointment.getStart(), workbook.getStyles().getDateTimeStyle());
		dataRow.addCell(col++, appointment.getEnd(), workbook.getStyles().getDateTimeStyle());
		dataRow.addCell(col++, AppointmentsUIFactory.getDisplayLocation(translator, appointment));
		String status = Status.confirmed == appointment.getStatus()
				? translator.translate("appointment.status.confirmed")
				: translator.translate("appointment.status.planned");
		dataRow.addCell(col++, status);
		
		User user = participation.getIdentity().getUser();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			dataRow.addCell(col++, userPropertyHandler.getUserProperty(user, translator.getLocale()));
		}
	}

}
