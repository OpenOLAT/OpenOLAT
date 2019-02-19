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
package org.olat.modules.lecture.ui.coach;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 21 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesStatisticsExport extends OpenXMLWorkbookResource {
	
	private static final OLog log = Tracing.createLoggerFor(LecturesStatisticsExport.class);
	
	private final Translator translator;
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final List<LectureBlockIdentityStatistics> statistics;
	private final Curriculum curriculum;
	private final CurriculumElement curriculumElement;
	
	private final LectureService lectureService;
	
	/**
	 * 
	 * @param statistics The raw statistics
	 * @param curriculum Add a title line
	 * @param curriculumElement Add a title line
	 * @param userPropertyHandlers The list of user property handlers
	 * @param isAdministrativeUser If the user which initiated the download has administrative rights
	 * @param translator the translator
	 */
	public LecturesStatisticsExport(List<LectureBlockIdentityStatistics> statistics,
			Curriculum curriculum, CurriculumElement curriculumElement,
			List<UserPropertyHandler> userPropertyHandlers, boolean isAdministrativeUser, Translator translator) {
		super(label());
		this.translator = translator;
		this.statistics = statistics;
		this.curriculum = curriculum;
		this.curriculumElement = curriculumElement;
		this.isAdministrativeUser = isAdministrativeUser;
		this.userPropertyHandlers = userPropertyHandlers;
		lectureService = CoreSpringFactory.getImpl(LectureService.class);
	}
	
	private static final String label() {
		return "ExportLectureStatistics_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
	}

	@Override
	protected void generate(OutputStream out) {
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 2)) {
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			exportSheet.setHeaderRows(curriculum == null ? 1 : 2);
			addHeadersCurriculum(exportSheet, workbook);
			addHeadersAggregated(exportSheet);
			addContentAggregated(exportSheet, workbook);
			
			exportSheet = workbook.nextWorksheet();
			exportSheet.setHeaderRows(curriculum == null ? 1 : 2);
			addHeadersCurriculum(exportSheet, workbook);
			addHeadersDetailled(exportSheet);
			addContentDetailled(exportSheet, workbook);
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	private void addHeadersCurriculum(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		if(curriculum == null) return;
		
		Row headerRow = exportSheet.newRow();
		headerRow.addCell(0, "Curriculum", workbook.getStyles().getHeaderStyle());
		headerRow.addCell(1, curriculum.getDisplayName());
		headerRow.addCell(2, curriculum.getIdentifier());
		
		if(curriculumElement != null) {
			headerRow.addCell(3, "Element", workbook.getStyles().getHeaderStyle());
			headerRow.addCell(4, curriculumElement.getDisplayName());
			headerRow.addCell(5, curriculumElement.getIdentifier());
		}
	}
	
	private void addHeadersAggregated(OpenXMLWorksheet exportSheet) {
		Row headerRow = exportSheet.newRow();
		
		int pos = 0;
		pos = addHeadersUser(headerRow, pos);
		addHeadersStatistics(headerRow, pos);
	}
	
	private void addHeadersDetailled(OpenXMLWorksheet exportSheet) {
		Row headerRow = exportSheet.newRow();
		
		int pos = 0;
		pos = addHeadersUser(headerRow, pos);
		headerRow.addCell(pos++, translator.translate("table.header.external.ref"));
		headerRow.addCell(pos++, translator.translate("table.header.entry"));
		addHeadersStatistics(headerRow, pos);
	}
	
	private int addHeadersUser(Row headerRow, int pos) {
		if(isAdministrativeUser) {
			headerRow.addCell(pos++, translator.translate("table.header.username"));
		}
		
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			headerRow.addCell(pos++, translator.translate("form.name." + userPropertyHandler.getName()));
		}
		return pos;
	}

	private int addHeadersStatistics(Row headerRow, int pos) {
		headerRow.addCell(pos++, translator.translate("table.header.planned.lectures"));
		headerRow.addCell(pos++, translator.translate("table.header.attended.lectures"));
		headerRow.addCell(pos++, translator.translate("table.header.absent.lectures"));
		headerRow.addCell(pos++, translator.translate("table.header.authorized.absence"));
		headerRow.addCell(pos++, translator.translate("table.header.attended.current.rate"));
		return pos;
	}
	
	private void addContentAggregated(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		List<LectureBlockIdentityStatistics> aggregatedStatistics = lectureService.groupByIdentity(statistics);
		for(LectureBlockIdentityStatistics statistic:aggregatedStatistics) {
			Row row = exportSheet.newRow();
			
			int pos = 0;
			pos = addContentUser(statistic, row, pos);
			addContentStatistics(statistic, row, pos, workbook);
		}
	}

	private void addContentDetailled(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {

		for(LectureBlockIdentityStatistics statistic:statistics) {
			Row row = exportSheet.newRow();
			
			int pos = 0;
			pos = addContentUser(statistic, row, pos);
			row.addCell(pos++, statistic.getExternalRef());
			row.addCell(pos++, statistic.getDisplayName());
			addContentStatistics(statistic, row, pos, workbook);
		}
	}

	private int addContentUser(LectureBlockIdentityStatistics statistic, Row row, int pos) {
		if(isAdministrativeUser) {
			row.addCell(pos++, statistic.getIdentityName());
		}
		
		int count = 0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			row.addCell(pos++, statistic.getIdentityProp(count++));
		}
		return pos;
	}

	private int addContentStatistics(LectureBlockIdentityStatistics statistic, Row row, int pos, OpenXMLWorkbook workbook) {
		row.addCell(pos++, positive(statistic.getTotalPersonalPlannedLectures()), null);
		row.addCell(pos++, positive(statistic.getTotalAttendedLectures()), null);
		row.addCell(pos++, positive(statistic.getTotalAbsentLectures()), null);
		row.addCell(pos++, positive(statistic.getTotalAuthorizedAbsentLectures()), null);
		row.addCell(pos++, statistic.getAttendanceRate(), workbook.getStyles().getPercentStyle());
		return pos;
	}
	
	private long positive(long val) {
		return val < 0 ? 0 : val;
	}
}
