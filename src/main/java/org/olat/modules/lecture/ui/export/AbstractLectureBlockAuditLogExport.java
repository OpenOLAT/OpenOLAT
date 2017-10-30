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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * 
 * 
 * 
 * Initial date: 12 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractLectureBlockAuditLogExport extends OpenXMLWorkbookResource {
	
	private static final OLog log = Tracing.createLoggerFor(LectureBlockAuditLogExport.class);
	
	protected final Translator translator;

	private final boolean authorizedAbsenceEnabled;
	private final List<LectureBlockAuditLog> auditLog;
	private Map<Long,String> displayNames = new HashMap<>();
	private Map<Long,String> lectureBlockTitles = new HashMap<>();
	
	protected final UserManager userManager;
	protected final LectureService lectureService;
	protected final RepositoryManager repositoryManager;
	
	public AbstractLectureBlockAuditLogExport(String name, List<LectureBlockAuditLog> auditLog,
			boolean authorizedAbsenceEnabled, Translator translator) {
		super(name);
		this.auditLog = auditLog;
		this.translator = translator;
		this.authorizedAbsenceEnabled = authorizedAbsenceEnabled;
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		lectureService = CoreSpringFactory.getImpl(LectureService.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
	}

	@Override
	protected void generate(OutputStream out) {
		Collections.sort(auditLog, new LectureBlockAuditLogComparator());
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			addSheetSettings(exportSheet);
			addHeaders(exportSheet);
			addHeader(exportSheet);
			addContent(exportSheet, workbook);
		} catch (IOException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	protected void addSheetSettings(OpenXMLWorksheet exportSheet) {
		exportSheet.setColumnWidth(1, 16);//width date time
		exportSheet.setColumnWidth(8, 16);//width date time
	}
	
	protected abstract void addHeaders(OpenXMLWorksheet exportSheet);
	
	private void addHeader(OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(2);
		
		Row headerRow = exportSheet.newRow();
		
		int pos = 0;
		headerRow.addCell(pos++, translator.translate("table.header.date"));//creationDate
		headerRow.addCell(pos++, translator.translate("table.header.log.action"));//action
		
		headerRow.addCell(pos++, translator.translate("table.header.entry"));//repository entry title
		headerRow.addCell(pos++, translator.translate("table.header.lecture.block"));//lecture block title
		
		//audit block
		headerRow.addCell(pos++, translator.translate("table.header.status"));//lecture block status
		headerRow.addCell(pos++, translator.translate("table.header.log.planned.lectures"));//lecture block planned lectures
		headerRow.addCell(pos++, translator.translate("table.header.log.effective.lectures"));//lecture block effective lectures
		headerRow.addCell(pos++, translator.translate("table.header.log.effective.end.date"));//lecture block effective end date
		
		//audit roll call
		headerRow.addCell(pos++, translator.translate("table.header.log.user"));//roll call user
		headerRow.addCell(pos++, translator.translate("table.header.attended.lectures"));//roll call attended
		headerRow.addCell(pos++, translator.translate("table.header.absent.lectures"));//roll call absent
		if(authorizedAbsenceEnabled) {
			headerRow.addCell(pos++, translator.translate("table.header.authorized.absence"));//roll call authorized
			headerRow.addCell(pos++, translator.translate("authorized.absence.reason"));//roll call reason
		}
		headerRow.addCell(pos++, translator.translate("rollcall.comment"));//roll call comment
		
		//author
		headerRow.addCell(pos++, translator.translate("table.header.log.author"));//author
	}
	
	private void addContent(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		
		for(LectureBlockAuditLog logEntry:auditLog) {
			int pos = 0;
			Row row = exportSheet.newRow();
			Date creationDate = logEntry.getCreationDate();
			row.addCell(pos++, creationDate, workbook.getStyles().getDateTimeStyle());
			row.addCell(pos++, logEntry.getAction());
			
			//repo entry title
			row.addCell(pos++, getRepositoryEntryDisplayName(logEntry.getEntryKey()), null);
			//lecture block
			row.addCell(pos++, getLectureBlockTitle(logEntry.getLectureBlockKey()), null);
			//date start / end
			
			//planned / effective
			LectureBlock auditBlock = null;
			LectureBlockRollCall auditRollCall = null;
			if(logEntry.getRollCallKey() != null) {
				auditRollCall = getAuditRollCall(logEntry.getAfter());
			}
			if(auditRollCall == null) {
				auditBlock = getAuditLectureBlock(logEntry.getAfter());
			}
			
			if(auditBlock != null) {
				if(auditBlock.getStatus() == null) {
					pos++;
				} else {
					row.addCell(pos++, auditBlock.getStatus().name(), null);
				}
				row.addCell(pos++, auditBlock.getPlannedLecturesNumber(), null);
				row.addCell(pos++, auditBlock.getEffectiveLecturesNumber(), null);
				row.addCell(pos++, auditBlock.getEffectiveEndDate(), workbook.getStyles().getDateTimeStyle());
			} else {
				pos += 4;
			}
			
			if(auditRollCall != null) {
				Long assessedIdentityKey = logEntry.getIdentityKey();
				String fullname = userManager.getUserDisplayName(assessedIdentityKey);
				row.addCell(pos++, fullname);
				row.addCell(pos++, auditRollCall.getLecturesAttendedNumber(), null);
				row.addCell(pos++, auditRollCall.getLecturesAbsentNumber(), null);
				if(authorizedAbsenceEnabled) {
					if(auditRollCall.getAbsenceAuthorized() != null && auditRollCall.getAbsenceAuthorized().booleanValue()) {
						row.addCell(pos++, "x");
					} else {
						pos++;
					}
					row.addCell(pos++, auditRollCall.getAbsenceReason(), null);
				}
				row.addCell(pos++, auditRollCall.getComment(), null);
			} else {
				pos += 4;
				if(authorizedAbsenceEnabled) {
					pos += 2;
				}
			}
			
			Long authorKey = logEntry.getAuthorKey();
			if(authorKey != null) {
				String fullname = userManager.getUserDisplayName(authorKey);
				row.addCell(pos++, fullname);
			}
		}
	}
	
	private LectureBlockRollCall getAuditRollCall(String xml) {
		return lectureService.toAuditLectureBlockRollCall(xml);
	}
	
	private LectureBlock getAuditLectureBlock(String xml) {
		return lectureService.toAuditLectureBlock(xml);
	}
	
	protected void cacheRepositoryEntry(RepositoryEntry entry) {
		if(entry != null) {
			displayNames.put(entry.getKey(), entry.getDisplayname());
		}
	}
	
	private String getRepositoryEntryDisplayName(Long entryKey) {
		
		String displayName = displayNames.get(entryKey);
		if(displayName == null) {
			displayName = repositoryManager.lookupDisplayName(entryKey);
			if(!StringHelper.containsNonWhitespace(displayName)) {
				displayName = entryKey.toString();
			}
			displayNames.put(entryKey, displayName);
		}
		return displayName;
	}
	
	protected void cacheLectureBlock(LectureBlock lectureBlock) {
		if(lectureBlock != null) {
			lectureBlockTitles.put(lectureBlock.getKey(), lectureBlock.getTitle());
		}
	}
	
	private String getLectureBlockTitle(Long lectureBlockKey) {
		String title = lectureBlockTitles.get(lectureBlockKey);
		if(title == null) {
			LectureBlock block = lectureService.getLectureBlock(new LectureBlockRefForLog(lectureBlockKey));
			if(block == null) {
				title = lectureBlockKey.toString();
			} else {
				title = block.getTitle();
			}
			lectureBlockTitles.put(lectureBlockKey, title);
		}
		return title;
	}
	
	private static class LectureBlockRefForLog implements LectureBlockRef {
		
		private Long key;
		
		public LectureBlockRefForLog(Long key) {
			this.key = key;
		}

		@Override
		public Long getKey() {
			return key;
		}
	}
}
