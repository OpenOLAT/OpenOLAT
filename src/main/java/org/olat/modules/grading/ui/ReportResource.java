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
package org.olat.modules.grading.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.openxml.workbookstyle.CellStyle;
import org.olat.modules.grading.GraderStatus;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.model.GraderWithStatistics;
import org.olat.modules.grading.model.GradersSearchParameters;
import org.olat.modules.grading.model.GradingAssignmentSearchParameters;
import org.olat.modules.grading.model.GradingAssignmentWithInfos;
import org.olat.modules.grading.ui.component.GraderStatusCellRenderer;
import org.olat.modules.grading.ui.component.GradingDeadlineStatusCellRenderer;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReportResource extends OpenXMLWorkbookResource {
	
	private static final Logger log = Tracing.createLoggerFor(ReportResource.class);
	
	private final Date from;
	private final Date to;
	private final Identity grader;
	private final Identity manager;
	private final RepositoryEntry referenceEntry;
	
	private final Translator translator;
	private final List<UserPropertyHandler> graderPropertyHandlers;
	private final GradingDeadlineStatusCellRenderer statusRenderer;
	private final List<UserPropertyHandler> assessedUserPropertyHandlers;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public ReportResource(Roles roles, String label, Date from, Date to,
			RepositoryEntry referenceEntry, Identity grader, Identity manager,
			Translator translator) {
		super(label);
		this.from = from;
		this.to = to;
		this.grader = grader;
		this.manager = manager;
		this.referenceEntry = referenceEntry;
		
		statusRenderer = new GradingDeadlineStatusCellRenderer(translator);
		this.translator = userManager.getPropertyHandlerTranslator(translator);
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		graderPropertyHandlers = userManager.getUserPropertyHandlersFor(GradersListController.USER_PROPS_ID, isAdministrativeUser);
		assessedUserPropertyHandlers = userManager.getUserPropertyHandlersFor(GradingAssignmentsListController.ASSESSED_PROPS_ID, isAdministrativeUser);
	}

	@Override
	protected void generate(OutputStream out) {
		try (OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 2)) {
			OpenXMLWorksheet gradersSheet = workbook.nextWorksheet();
			createGradersHeader(gradersSheet, workbook);
			createGradersData(gradersSheet, workbook);
			
			OpenXMLWorksheet assignmentsSheet = workbook.nextWorksheet();
			createAssignmentsHeader(assignmentsSheet, workbook);
			createAssignmentsData(assignmentsSheet, workbook);
		} catch (Exception e) {
			log.error("Unable to export report", e);
		}
	}

	private void createGradersHeader(OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
		sheet.setHeaderRows(1);
		Row headerRow = sheet.newRow();
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();
		
		int pos = 0;
		for (UserPropertyHandler userPropertyHandler:graderPropertyHandlers) {
			headerRow.addCell(pos++, translator.translate(userPropertyHandler.i18nColumnDescriptorLabelKey()), headerStyle);
		}

		headerRow.addCell(pos++, translator.translate("table.header.status"), headerStyle);
		headerRow.addCell(pos++, translator.translate("table.header.assignments.total"), headerStyle);
		headerRow.addCell(pos++, translator.translate("table.header.assignments.done"), headerStyle);
		headerRow.addCell(pos++, translator.translate("table.header.assignments.open"), headerStyle);
		headerRow.addCell(pos++, translator.translate("table.header.assignments.overdue"), headerStyle);
		headerRow.addCell(pos++, translator.translate("table.header.assignments.oldest.open"), headerStyle);
		headerRow.addCell(pos++, translator.translate("table.header.recorded.meta.time"), headerStyle);
		headerRow.addCell(pos, translator.translate("table.header.recorded.time"), headerStyle);
	}
	
	private void createGradersData(GraderWithStatistics graderStatistics, OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
		Row row = sheet.newRow();
		Identity graderIdentity = graderStatistics.getGrader();
		
		int pos = 0;
		for (UserPropertyHandler userPropertyHandler:graderPropertyHandlers) {
			String val = userPropertyHandler.getUserProperty(graderIdentity.getUser(), translator.getLocale());
			row.addCell(pos++, val);
		}

		GraderStatus graderStatus = GraderStatusCellRenderer.getFinalStatus(graderStatistics.getGraderStatus());
		if(graderStatus == null) {
			pos++;
		} else {
			row.addCell(pos++, translator.translate("grader.status.".concat(graderStatus.name())));
		}
		row.addCell(pos++, graderStatistics.getStatistics().getTotalAssignments(), null);
		row.addCell(pos++, graderStatistics.getStatistics().getNumOfDoneAssignments(), null);
		row.addCell(pos++, graderStatistics.getStatistics().getNumOfOpenAssignments(), null);
		row.addCell(pos++, graderStatistics.getStatistics().getNumOfOverdueAssignments(), null);
		row.addCell(pos++, graderStatistics.getStatistics().getOldestOpenAssignment(), workbook.getStyles().getDateStyle());
		row.addCell(pos++, CalendarUtils.convertSecondsToMinutes(graderStatistics.getRecordedMetadataTimeInSeconds()), null);
		row.addCell(pos, CalendarUtils.convertSecondsToMinutes(graderStatistics.getRecordedTimeInSeconds()), null);
	}
	
	private void createGradersData(OpenXMLWorksheet gradersSheet, OpenXMLWorkbook workbook) {
		GradersSearchParameters searchParams = new GradersSearchParameters();
		searchParams.setClosedFromDate(from);
		searchParams.setClosedToDate(to);
		searchParams.setManager(manager);
		searchParams.setGrader(grader);
		searchParams.setReferenceEntry(referenceEntry);

		List<GraderWithStatistics> statistics = gradingService.getGradersWithStatistics(searchParams);
		dbInstance.commitAndCloseSession();
		for(GraderWithStatistics graderStatistics:statistics) {
			createGradersData(graderStatistics, gradersSheet, workbook);
		}	
	}
	
	private void createAssignmentsHeader(OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
		sheet.setHeaderRows(1);
		Row headerRow = sheet.newRow();
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();
		
		// grader informations
		int pos = 0;
		for (UserPropertyHandler userPropertyHandler:graderPropertyHandlers) {
			headerRow.addCell(pos++, translator.translate(userPropertyHandler.i18nColumnDescriptorLabelKey()), headerStyle);
		}

		headerRow.addCell(pos++, translator.translate("table.header.deadline"), headerStyle);
		
		// assessed identities informations
		for (UserPropertyHandler userPropertyHandler:assessedUserPropertyHandlers) {
			headerRow.addCell(pos++, translator.translate(userPropertyHandler.i18nColumnDescriptorLabelKey()), headerStyle);
		}
		if(organisationModule.isEnabled()) {
			headerRow.addCell(pos++, translator.translate("table.header.organisation"), headerStyle);
		}
		// entry (courses) informations 
		headerRow.addCell(pos++, translator.translate("table.header.entry"), headerStyle);
		headerRow.addCell(pos++, translator.translate("table.header.entry.external.ref"), headerStyle);
		if(taxonomyModule.isEnabled()) {
			headerRow.addCell(pos++, translator.translate("table.header.taxonomy"), headerStyle);
		}
		headerRow.addCell(pos++, translator.translate("table.header.course.element"), headerStyle);
		
		// assessment infos
		headerRow.addCell(pos++, translator.translate("table.header.assessment.date"), headerStyle);
		headerRow.addCell(pos++, translator.translate("table.header.correction.meta.minutes"), headerStyle);
		headerRow.addCell(pos++, translator.translate("table.header.correction.minutes"), headerStyle);
		headerRow.addCell(pos++, translator.translate("table.header.assignment.date"), headerStyle);
		headerRow.addCell(pos++, translator.translate("table.header.done.date"), headerStyle);
		headerRow.addCell(pos++, translator.translate("table.header.score"), headerStyle);
		headerRow.addCell(pos, translator.translate("table.header.passed"), headerStyle);
	}

	private void createAssignmentsData(GradingAssignmentWithInfos assignmentWithInfos, Map<Long,List<String>> organisationsMap,
			OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
		Row row = sheet.newRow();
		
		int pos = 0;
		// grader name
		GradingAssignment assignment = assignmentWithInfos.getAssignment();
		if(assignment.getGrader() == null || assignment.getGrader().getIdentity() == null) {
			pos += graderPropertyHandlers.size();
		} else {
			Identity graderIdentity = assignment.getGrader().getIdentity();
			for (UserPropertyHandler userPropertyHandler:graderPropertyHandlers) {
				String val = userPropertyHandler.getUserProperty(graderIdentity.getUser(), translator.getLocale());
				row.addCell(pos++, val);
			}
		}
		
		// deadline
		try(StringOutput status = new StringOutput(32)) {
			statusRenderer.render(null, status, assignment.getDeadline(), assignment.getExtendedDeadline(),
					assignment.getAssignmentStatus());
			row.addCell(pos++, status.toString());
		} catch(IOException e) {
			pos++;
		}
		
		// assessed identities informations
		Identity assessedIdentity = assignmentWithInfos.getAssessedIdentity();
		for (UserPropertyHandler userPropertyHandler:assessedUserPropertyHandlers) {
			String val = userPropertyHandler.getUserProperty(assessedIdentity.getUser(), translator.getLocale());
			row.addCell(pos++, val);
		}
		
		if(organisationModule.isEnabled()) {
			List<String> orgs = organisationsMap.get(assessedIdentity.getKey());
			if(!orgs.isEmpty()) {
				row.addCell(pos, toString(orgs));
			}
			pos++;
		}
		
		// entry (course) informations
		RepositoryEntry entry = assignmentWithInfos.getEntry();
		row.addCell(pos++, entry.getDisplayname());
		row.addCell(pos++, entry.getExternalRef());
		if(taxonomyModule.isEnabled()) {
			row.addCell(pos++, assignmentWithInfos.getTaxonomyLevels());
		}

		row.addCell(pos++, assignmentWithInfos.getCourseElementTitle());
		row.addCell(pos++, assignmentWithInfos.getAssessmentDate(), workbook.getStyles().getDateStyle());
		row.addCell(pos++, CalendarUtils.convertSecondsToMinutes(assignmentWithInfos.getMetadataTimeRecordedInSeconds()), null);
		row.addCell(pos++, CalendarUtils.convertSecondsToMinutes(assignmentWithInfos.getTimeRecordedInSeconds()), null);
		row.addCell(pos++, assignment.getAssignmentDate(), workbook.getStyles().getDateStyle());
		row.addCell(pos++, assignment.getClosingDate(), workbook.getStyles().getDateStyle());
		row.addCell(pos++, assignmentWithInfos.getScore(), null);
		
		Boolean passed = assignmentWithInfos.getPassed();
		if(passed != null && passed.booleanValue()) {
			row.addCell(pos, translator.translate("passed.true.label"));
		} else if(passed != null && !passed.booleanValue()) {
			row.addCell(pos, translator.translate("passed.false.label"));
		}
	}
	
	private String toString(List<String> names) {
		if(names.size() > 1) {
			Collections.sort(names);
		}
		
		StringBuilder sb = new StringBuilder(32);
		for(String name:names) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(name);
		}
		return sb.toString();
	}
	
	private void createAssignmentsData(OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
		GradingAssignmentSearchParameters searchParams = new GradingAssignmentSearchParameters();
		searchParams.setClosedFromDate(from);
		searchParams.setClosedToDate(to);
		searchParams.setGrader(grader);
		searchParams.setManager(manager);
		searchParams.setReferenceEntry(referenceEntry);
		
		List<GradingAssignmentWithInfos> assignmentsWithInfos = gradingService.getGradingAssignmentsWithInfos(searchParams, translator.getLocale());
		List<IdentityRef> assessedIdentities = assignmentsWithInfos.stream()
				.map(GradingAssignmentWithInfos::getAssessedIdentity)
				.collect(Collectors.toList());
		Map<Long,List<String>> organisationsMap = organisationService.getUsersOrganisationsNames(assessedIdentities);
		for(GradingAssignmentWithInfos assignmentWithInfos:assignmentsWithInfos) {
			createAssignmentsData(assignmentWithInfos, organisationsMap, sheet, workbook);
		}
	}
}
