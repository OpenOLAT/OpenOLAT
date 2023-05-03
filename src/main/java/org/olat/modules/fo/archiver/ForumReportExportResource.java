/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.fo.archiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FOCourseNode;

/**
 * creating xlsx report for forums
 * <p>
 * Initial date: Apr 17, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ForumReportExportResource implements MediaResource {

	private static final Logger log = Tracing.createLoggerFor(ForumReportExportResource.class);
	private static final String ENCODING = "UTF-8";

	private final List<CourseNode> courseNodes;
	private final ICourse course;
	private final Translator translator;
	private final Date beginDate;
	private final Date endDate;
	private final List<String> selectedOrgaKeys;

	public ForumReportExportResource(List<CourseNode> courseNodes, ICourse course, Translator translator,
									 Date beginDate, Date endDate, List<String> selectedOrgaKeys) {
		this.courseNodes = courseNodes;
		this.course = course;
		this.translator = translator;
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.selectedOrgaKeys = selectedOrgaKeys;
	}

	private void createHeader(OpenXMLWorksheet worksheet) {
		OpenXMLWorksheet.Row headerRow = worksheet.newRow();
		int col = 0;

		headerRow.addCell(col++, translator.translate("fo.report.fo.name"));
		headerRow.addCell(col++, translator.translate("fo.report.id"));
		headerRow.addCell(col++, translator.translate("fo.report.thread"));
		headerRow.addCell(col++, translator.translate("fo.report.title"));
		headerRow.addCell(col++, translator.translate("fo.report.creation.date"));
		headerRow.addCell(col++, translator.translate("fo.report.last.modified.date"));
		headerRow.addCell(col++, translator.translate("fo.report.creator.firstname"));
		headerRow.addCell(col++, translator.translate("fo.report.creator.lastname"));
		headerRow.addCell(col++, translator.translate("fo.report.creator.nickname"));
		headerRow.addCell(col++, translator.translate("fo.report.organisations"));
		headerRow.addCell(col++, translator.translate("fo.report.word.count"));
		headerRow.addCell(col, translator.translate("fo.report.char.count"));
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding(ENCODING);
		} catch (Exception e) {
			log.error("Error setting character encoding for response failed.", e);
		}

		// build up export FileName
		String label = StringHelper.transformDisplayNameToFileSystemName(course.getCourseTitle())
				+ "_" + Formatter.formatDatetimeWithMinutes(new Date()) + ".xlsx";
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + urlEncodedLabel);
		hres.setHeader("Content-Description", urlEncodedLabel);

		try (OutputStream out = hres.getOutputStream();
			 OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);

			createHeader(sheet);

			// createdata <-> create rows
			for (CourseNode courseNode : courseNodes) {
				FOCourseNode forumNode = (FOCourseNode) courseNode;
				ForumReportExportData foReportExportData = new ForumReportExportData(forumNode, course, beginDate, endDate, selectedOrgaKeys);
				foReportExportData.createExportData(sheet);
			}
		} catch (IOException e) {
			log.error("Unable to export forum reports as xlsx: {}", e.getMessage());
		}
	}

	@Override
	public void release() {
		// no need
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public long getCacheControlDuration() {
		return 0;
	}
}
