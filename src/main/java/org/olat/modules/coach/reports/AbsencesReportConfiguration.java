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

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.modules.lecture.LectureModule;
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

		List<String> worksheetNames = new ArrayList<>();
		worksheetNames.add(translator.translate("export.worksheet.summary"));
		worksheetNames.add(translator.translate("export.worksheet.courses"));
		List<UserPropertyHandler> userPropertyHandlers = getUserPropertyHandlers();
		List<LectureBlockIdentityStatistics> statistics = getLecturesStatistics(userPropertyHandlers, coach);
		LectureModule lectureModule = CoreSpringFactory.getImpl(LectureModule.class);
		boolean countAuthorizedAbsenceAsAttendant = lectureModule.isCountAuthorizedAbsenceAsAttendant();
		boolean countDispenseAsAttendant = lectureModule.isCountDispensationAsAttendant() && lectureModule.isAbsenceNoticeEnabled();

		try (OutputStream out = new FileOutputStream(output.getBasefile());
			 OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, worksheetNames.size(), worksheetNames)) {

			OpenXMLWorksheet summaryWorksheet = workbook.nextWorksheet();
			generateSummaryHeader(summaryWorksheet, userPropertyHandlers, translator);
			generateSummaryData(summaryWorksheet, userPropertyHandlers, statistics, workbook,
					countAuthorizedAbsenceAsAttendant, countDispenseAsAttendant);
			
			OpenXMLWorksheet coursesWorksheet = workbook.nextWorksheet();
			generateCoursesHeader(coursesWorksheet, userPropertyHandlers, translator);
			generateCoursesData(coursesWorksheet, userPropertyHandlers, statistics, workbook,
					countAuthorizedAbsenceAsAttendant, countDispenseAsAttendant);
		} catch (IOException e) {
			log.error("Unable to generate export", e);
		}
	}

	private List<LectureBlockIdentityStatistics> getLecturesStatistics(List<UserPropertyHandler> userPropertyHandlers, Identity coach) {
		LectureService lectureService = CoreSpringFactory.getImpl(LectureService.class);
		LectureStatisticsSearchParameters params = new LectureStatisticsSearchParameters();
		params.setLimitToRoles(List.of(OrganisationRoles.linemanager, OrganisationRoles.educationmanager));
		if (getDurationTimeUnit() != null) {
			int duration = getDuration() != null ? Integer.parseInt(getDuration()) : 0;
			params.setStartDate(getDurationTimeUnit().fromDate(new Date(), duration));
			params.setEndDate(getDurationTimeUnit().toDate(new Date()));
		}
		return lectureService.getLecturesStatistics(params, userPropertyHandlers, coach);
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
									 List<LectureBlockIdentityStatistics> statistics, OpenXMLWorkbook workbook, 
									 boolean countAuthorizedAbsenceAsAttendant, boolean countDispenseAsAttendant) {
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
			long absent = 0;
			for (LectureBlockIdentityStatistics stats : statisticsForIdentity) {
				if (identityProps == null) {
					identityProps = stats.getIdentityProps();
				}
				units += stats.getTotalPersonalPlannedLectures();
				attended += stats.getTotalAttendedLectures();
				absent += stats.getTotalAbsentLectures();
				if (countAuthorizedAbsenceAsAttendant) {
					attended += stats.getTotalAuthorizedAbsentLectures();
					attended += stats.getTotalDispensationLectures();
				} else if (countDispenseAsAttendant) {
					absent += stats.getTotalAuthorizedAbsentLectures();
					attended += stats.getTotalDispensationLectures();
				} else {
					absent += stats.getTotalAuthorizedAbsentLectures();
					absent += stats.getTotalDispensationLectures();
				}
			}

			if (absent < 0) {
				absent = 0;
			}
			
			long total = attended + absent;
			double rate;
			if (total == 0 || attended == 0) {
				rate = 0.0d;
			} else {
				rate = (double) attended / (double) total;
			}
			summaryRows.add(new AbsencesReportSummaryRow(identityProps, positive(units), positive(attended), rate));
		}
		
		for (AbsencesReportSummaryRow summaryRow : summaryRows) {
			Row row = worksheet.newRow();
			int pos = 0;

			generateSummaryDataRow(row, pos, userPropertyHandlers, summaryRow, workbook);
		}
	}

	private void generateSummaryDataRow(Row row, int pos, List<UserPropertyHandler> userPropertyHandlers,
										AbsencesReportSummaryRow summaryRow, OpenXMLWorkbook workbook) {
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			row.addCell(pos, summaryRow.getIdentityProp(pos));
			pos++;
		}
		row.addCell(pos++, "" + summaryRow.units());
		row.addCell(pos++, "" + summaryRow.attended());
		row.addCell(pos++, summaryRow.attendanceRate(), workbook.getStyles().getPercentStyle());
	}

	private void generateCoursesHeader(OpenXMLWorksheet worksheet, List<UserPropertyHandler> userPropertyHandlers, 
									   Translator translator) {
		worksheet.setHeaderRows(1);
		Row header = worksheet.newRow();
		int pos = 0;

		pos = generateUserHeaderColumns(header, pos, userPropertyHandlers, translator);
		
		header.addCell(pos++, translator.translate("export.header.course.title"));
		header.addCell(pos++, translator.translate("export.header.externalReference"));
		header.addCell(pos++, translator.translate("export.header.units"));
		header.addCell(pos++, translator.translate("export.header.attended"));
		header.addCell(pos++, translator.translate("export.header.authorized"));
		header.addCell(pos++, translator.translate("export.header.not.authorized"));
		header.addCell(pos++, translator.translate("export.header.dispensed"));
		header.addCell(pos++, translator.translate("export.header.attendance.rate"));
	}

	private void generateCoursesData(OpenXMLWorksheet worksheet, List<UserPropertyHandler> userPropertyHandlers,
									 List<LectureBlockIdentityStatistics> statistics, OpenXMLWorkbook workbook, 
									 boolean countAuthorizedAbsenceAsAttendant, boolean countDispenseAsAttendant) {
		
		Map<UserCourseKey, List<LectureBlockIdentityStatistics>> statisticsMap = new HashMap<>();
		for (LectureBlockIdentityStatistics stats : statistics) {
			Long identityKey = stats.getIdentityKey();
			Long courseKey = stats.getRepoKey();
			if (identityKey == null || courseKey == null) {
				continue;
			}
			UserCourseKey userCourseKey = new UserCourseKey(identityKey, courseKey);
			statisticsMap.computeIfAbsent(userCourseKey, k -> new ArrayList<>()).add(stats);	
		}
		
		List<AbsencesReportCourseRow> userCourseRows = new ArrayList<>();
		for (UserCourseKey userCourseKey : statisticsMap.keySet()) {
			List<LectureBlockIdentityStatistics> statisticsForKey = statisticsMap.get(userCourseKey);
			String[] identityProps = null;
			String courseTitle = null;
			String externalReference = null;
			long units = 0;
			long attended = 0;
			long absent = 0;
			long authorized = 0;
			long dispensed = 0;
			for (LectureBlockIdentityStatistics stats : statisticsForKey) {
				if (identityProps == null) {
					identityProps = stats.getIdentityProps();
				}
				if (courseTitle == null && StringHelper.containsNonWhitespace(stats.getDisplayName())) {
					courseTitle = stats.getDisplayName();
				}
				if (externalReference == null && StringHelper.containsNonWhitespace(stats.getExternalRef())) {
					externalReference = stats.getExternalRef();
				}
				units += stats.getTotalPersonalPlannedLectures();
				attended += stats.getTotalAttendedLectures();
				absent += stats.getTotalAbsentLectures();
				authorized += stats.getTotalAuthorizedAbsentLectures();
				dispensed += stats.getTotalDispensationLectures();
				if (countAuthorizedAbsenceAsAttendant) {
					attended += stats.getTotalAuthorizedAbsentLectures();
					attended += stats.getTotalDispensationLectures();
				} else if (countDispenseAsAttendant) {
					absent += stats.getTotalAuthorizedAbsentLectures();
					attended += stats.getTotalDispensationLectures();
				} else {
					absent += stats.getTotalAuthorizedAbsentLectures();
					absent += stats.getTotalDispensationLectures();
				}
			}
			
			if (absent < 0) {
				absent = 0;
			}
			
			long notAuthorized = units - attended - authorized - dispensed;
			long total = attended + absent;
			double rate;
			if (total == 0 || attended == 0) {
				rate = 0.0d;
			} else {
				rate = (double) attended / (double) total;
			}
			userCourseRows.add(new AbsencesReportCourseRow(identityProps, courseTitle, externalReference, 
					positive(units), positive(attended), positive(authorized), positive(notAuthorized), 
					positive(dispensed), rate));
		}
		
		for (AbsencesReportCourseRow userCourseRow : userCourseRows) {
			Row row = worksheet.newRow();
			int pos = 0;
			
			generateCourseDataRow(row, pos, userPropertyHandlers, userCourseRow, workbook);
		}
	}

	private static long positive(long value) {
		return value < 0 ? 0 : value;
	}

	private void generateCourseDataRow(Row row, int pos, List<UserPropertyHandler> userPropertyHandlers,
									   AbsencesReportCourseRow userCourseRow, OpenXMLWorkbook workbook) {
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			row.addCell(pos, userCourseRow.getIdentityProp(pos));
			pos++;
		}
		row.addCell(pos++, userCourseRow.courseTitle());
		row.addCell(pos++, userCourseRow.externalReference());
		row.addCell(pos++, "" + userCourseRow.units());
		row.addCell(pos++, "" + userCourseRow.attended());
		row.addCell(pos++, "" + userCourseRow.authorized());
		row.addCell(pos++, "" + userCourseRow.notAuthorized());
		row.addCell(pos++, "" + userCourseRow.dispensed());
		row.addCell(pos++, userCourseRow.attendanceRate(), workbook.getStyles().getPercentStyle());
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
