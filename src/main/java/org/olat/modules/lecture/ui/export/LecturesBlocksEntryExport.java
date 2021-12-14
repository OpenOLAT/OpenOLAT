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
package org.olat.modules.lecture.ui.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.Reason;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockWithTeachersComparator;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 6 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesBlocksEntryExport extends OpenXMLWorkbookResource {
	
	private static final Logger log = Tracing.createLoggerFor(LecturesBlocksEntryExport.class);
	
	private final Formatter formatter;
	private final RepositoryEntry entry;
	private final Translator translator;
	private List<LectureBlockWithTeachers> blocks;
	private final boolean authorizedAbsenceEnabled;
	private final boolean isAdministrativeUser;

	private final UserManager userManager;
	private final LectureService lectureService;
	
	public LecturesBlocksEntryExport(RepositoryEntry entry, boolean isAdministrativeUser, boolean authorizedAbsenceEnabled, Translator translator) {
		super(label(entry));
		this.entry = entry;
		this.translator = translator;
		this.isAdministrativeUser = isAdministrativeUser;
		this.authorizedAbsenceEnabled = authorizedAbsenceEnabled;
		
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		lectureService = CoreSpringFactory.getImpl(LectureService.class);
		formatter = Formatter.getInstance(translator.getLocale());
	}
	
	private static final String label(RepositoryEntry entry) {
		return StringHelper.transformDisplayNameToFileSystemName(entry.getDisplayname())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
	}

	@Override
	protected void generate(OutputStream out) {
		blocks = lectureService.getLectureBlocksWithTeachers(entry);
		Collections.sort(blocks, new LectureBlockWithTeachersComparator());
		
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1 + blocks.size())) {
			//overview of all lecture blocks
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			exportSheet.setHeaderRows(1);
			addHeaders(exportSheet);
			addContent(exportSheet);

			for(LectureBlockWithTeachers block:blocks) {
				OpenXMLWorksheet exportBlockSheet = workbook.nextWorksheet();
				LectureBlockExport lectureBlockExport = new LectureBlockExport(block.getLectureBlock(), block.getTeachers(),
						isAdministrativeUser, authorizedAbsenceEnabled, translator);
				lectureBlockExport.generate(exportBlockSheet);
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	private void addHeaders(OpenXMLWorksheet exportSheet) {
		Row headerRow = exportSheet.newRow();
		
		int pos = 0;
		headerRow.addCell(pos++, translator.translate("lecture.title"));
		headerRow.addCell(pos++, translator.translate("lecture.location"));
		headerRow.addCell(pos++, translator.translate("lecture.date"));
		headerRow.addCell(pos++, translator.translate("table.header.start.time"));
		headerRow.addCell(pos++, translator.translate("table.header.end.time"));
		headerRow.addCell(pos++, translator.translate("table.header.teachers"));
		headerRow.addCell(pos++, translator.translate("table.header.status"));
		headerRow.addCell(pos++, translator.translate("table.header.auto.close.date"));
		headerRow.addCell(pos++, translator.translate("planned.lectures"));
		headerRow.addCell(pos++, translator.translate("table.header.effective.lectures"));
		headerRow.addCell(pos++, translator.translate("lecture.block.effective.end"));
		headerRow.addCell(pos++, translator.translate("lecture.block.effective.reason"));
		headerRow.addCell(pos++, translator.translate("table.header.comment"));
	}
	
	private void addContent(OpenXMLWorksheet exportSheet) {
		for(LectureBlockWithTeachers block:blocks) {
				Row row = exportSheet.newRow();
				LectureBlock lectureBlock = block.getLectureBlock();
				
				int pos = 0;
				row.addCell(pos++, lectureBlock.getTitle());
				row.addCell(pos++, lectureBlock.getLocation());
				row.addCell(pos++, formatDate(lectureBlock.getStartDate()));
				row.addCell(pos++, formatTime(lectureBlock.getStartDate()));
				row.addCell(pos++, formatTime(lectureBlock.getEndDate()));
				
				StringBuilder teachers = new StringBuilder();
				for(Identity teacher:block.getTeachers()) {
					if(teachers.length() > 0) teachers.append(", ");
					teachers.append(userManager.getUserDisplayName(teacher));
				}
				row.addCell(pos++, teachers.toString());
				if(lectureBlock.getRollCallStatus() == null) {
					pos++;
				} else {
					String status = LectureBlockStatusCellRenderer.getStatus(lectureBlock, translator);
					if(status != null) {
						row.addCell(pos++, status);
					} else {
						pos++;
					}
				}
				row.addCell(pos++, formatter.formatDate(lectureBlock.getAutoClosedDate()));
				
				row.addCell(pos++, toInt(lectureBlock.getPlannedLecturesNumber()));
				row.addCell(pos++, toInt(lectureBlock.getEffectiveLecturesNumber()));
				row.addCell(pos++, formatTime(lectureBlock.getEffectiveEndDate()));
				
				Reason reason = lectureBlock.getReasonEffectiveEnd();
				if(reason == null) {
					pos++;
				} else {
					row.addCell(pos++, reason.getTitle());
				}
				row.addCell(pos++, lectureBlock.getComment());

		}
	}
	
	private String toInt(int number) {
		return number < 0 ? null : Integer.toString(number);
	}
	
	private String formatTime(Date time) {
		return time == null ? null : formatter.formatTimeShort(time);
	}
	
	private String formatDate(Date date) {
		return date == null ? null : formatter.formatDate(date);
	}
}
