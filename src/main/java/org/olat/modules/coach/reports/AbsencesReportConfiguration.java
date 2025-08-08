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
package org.olat.modules.coach.reports;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.model.LectureStatisticsSearchParameters;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: 2025-01-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AbsencesReportConfiguration extends TimeBoundReportConfiguration {

	@Override
	public boolean hasAccess(ReportConfigurationAccessSecurityCallback secCallback) {
		return secCallback.isCoachingContext() && secCallback.isShowAbsencesReports();
	}

	@Override
	protected String getI18nCategoryKey() {
		return "report.category.absences";
	}

	@Override
	public void generateReport(Identity coach, Locale locale, LocalFileImpl output) {
		Translator translator = getTranslator(locale);
		Formatter formatter = Formatter.getInstance(locale);

		List<String> worksheetNames = new ArrayList<>();
		worksheetNames.add(translator.translate("export.worksheet.summary"));
		worksheetNames.add(translator.translate("export.worksheet.courses"));
		List<UserPropertyHandler> userPropertyHandlers = getUserPropertyHandlers();

		try (OutputStream out = new FileOutputStream(output.getBasefile());
			 OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, worksheetNames.size(), worksheetNames)) {

			OpenXMLWorksheet summaryWorksheet = workbook.nextWorksheet();
			generateSummaryHeader(summaryWorksheet, userPropertyHandlers, translator);
			generateSummaryData(summaryWorksheet, userPropertyHandlers, formatter, translator, coach);
			
			OpenXMLWorksheet coursesWorksheet = workbook.nextWorksheet();
			generateCoursesHeader(coursesWorksheet, userPropertyHandlers, translator);
			generateCoursesData(coursesWorksheet, userPropertyHandlers, formatter, translator);
		} catch (IOException e) {
			log.error("Unable to generate export", e);
		}
	}
	
	private void generateSummaryHeader(OpenXMLWorksheet worksheet, List<UserPropertyHandler> userPropertyHandlers,  
									   Translator translator) {
		worksheet.setHeaderRows(1);
		Row header = worksheet.newRow();
		int pos = 0;
		
		pos = generateUserHeaderColumns(header, pos, userPropertyHandlers, translator);

		header.addCell(pos++, translator.translate("export.header.units"));
		header.addCell(pos++, translator.translate("export.header.attended"));
		header.addCell(pos++, translator.translate("export.header.attendance.rate"));
	}
	
	private void generateSummaryData(OpenXMLWorksheet worksheet, List<UserPropertyHandler> userPropertyHandlers,
									 Formatter formatter, Translator translator, Identity coach) {
		LectureService lectureService = CoreSpringFactory.getImpl(LectureService.class);
		LectureStatisticsSearchParameters params = new LectureStatisticsSearchParameters();
		if (getDurationTimeUnit() != null) {
			int duration = getDuration() != null ? Integer.parseInt(getDuration()) : 0;
			params.setStartDate(getDurationTimeUnit().fromDate(new Date(), duration));
			params.setEndDate(getDurationTimeUnit().toDate(new Date()));
		}
		List<LectureBlockIdentityStatistics> statistics = lectureService.getLecturesStatistics(params, userPropertyHandlers, coach);
		Map<Long, List<LectureBlockIdentityStatistics>> identityToStatistics = new HashMap<>();
		for (LectureBlockIdentityStatistics stats : statistics) {
			Long identityKey = stats.getIdentityKey();
			identityToStatistics.computeIfAbsent(identityKey, k -> new ArrayList<>()).add(stats);
		}
		List<AbsencesReportSummaryRow> summaryRows = new ArrayList<>();
		for (Long identityKey : identityToStatistics.keySet()) {
			List<LectureBlockIdentityStatistics> statisticsForIdentity = identityToStatistics.get(identityKey);
			String[] identityProps = null;
			long units = 0;
			long attended = 0;
			for (LectureBlockIdentityStatistics stats : statisticsForIdentity) {
				if (identityProps == null) {
					identityProps = stats.getIdentityProps();
				}
				units += stats.getTotalPersonalPlannedLectures();
				attended += stats.getTotalAttendedLectures();
			}
			
			double attendanceRate = units == 0 ? 0 : (double) attended / (double) units;
			summaryRows.add(new AbsencesReportSummaryRow(identityProps, units, attended, attendanceRate));
		}
		
		for (AbsencesReportSummaryRow summaryRow : summaryRows) {
			Row row = worksheet.newRow();
			int pos = 0;

			generateSummaryDataRow(row, pos, userPropertyHandlers, summaryRow);
		}
	}

	private void generateSummaryDataRow(Row row, int pos, List<UserPropertyHandler> userPropertyHandlers,
										AbsencesReportSummaryRow summaryRow) {
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			row.addCell(pos, summaryRow.getIdentityProp(pos));
			pos++;
		}
		row.addCell(pos++, "" + summaryRow.units());
		row.addCell(pos++, "" + summaryRow.attended());
		row.addCell(pos++, "" + summaryRow.attendanceRate());
	}

	private void generateCoursesHeader(OpenXMLWorksheet worksheet, List<UserPropertyHandler> userPropertyHandlers, 
									   Translator translator) {
		worksheet.setHeaderRows(1);
		Row header = worksheet.newRow();
		int pos = 0;

		pos = generateUserHeaderColumns(header, pos, userPropertyHandlers, translator);
	}

	private void generateCoursesData(OpenXMLWorksheet worksheet, List<UserPropertyHandler> userPropertyHandlers, 
									 Formatter formatter, Translator translator) {
		//
	}

	@Override
	protected int generateCustomHeaderColumns(Row header, int pos, Translator translator) {
		// Unused.
		// Two worksheets used, this hook is for a single worksheet.
		return 0;
	}

	@Override
	protected void generateData(OpenXMLWorkbook workbook, Identity coach, OpenXMLWorksheet sheet, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		// Unused.
		// Two worksheets used, this hook is for a single worksheet.
	}

	@Override
	protected List<UserPropertyHandler> getUserPropertyHandlers() {
		return CoreSpringFactory.getImpl(UserManager.class).getUserPropertyHandlersFor(PROPS_IDENTIFIER, false);
	}
}
