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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.ui.ParticipantListRepositoryController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 7 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockExport extends OpenXMLWorkbookResource {
	
	private static final Logger log = Tracing.createLoggerFor(LectureBlockExport.class);
	
	private final Translator translator;
	private final LectureBlock lectureBlock;
	private final LectureService lectureService;
	private final List<Identity> teachers;
	
	private final boolean authorizedAbsenceEnabled;
	private List<UserPropertyHandler> userPropertyHandlers;
	
	private final UserManager userManager;
	
	public LectureBlockExport(LectureBlock lectureBlock, List<Identity> teachers, boolean isAdministrativeUser, boolean authorizedAbsenceEnabled, Translator translator) {
		super(label(lectureBlock));
		this.teachers = teachers;
		this.lectureBlock = lectureBlock;
		lectureService = CoreSpringFactory.getImpl(LectureService.class);
		this.authorizedAbsenceEnabled = authorizedAbsenceEnabled;
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(ParticipantListRepositoryController.USER_PROPS_ID, isAdministrativeUser);
		this.translator = userManager.getPropertyHandlerTranslator(translator);
	}
	
	private static final String label(LectureBlock lectureBlock) {
		return StringHelper.transformDisplayNameToFileSystemName(lectureBlock.getTitle())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
	}

	@Override
	protected void generate(OutputStream out) {
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			generate(exportSheet);
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	protected void generate(OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(3);
		addHeaders_1(exportSheet);
		addHeaders_2(exportSheet);
		addHeaders_3(exportSheet);
		addContent(exportSheet);
		addFooter(exportSheet);
	}
	
	private void addFooter(OpenXMLWorksheet exportSheet) {
		exportSheet.newRow();
		exportSheet.newRow();
		Row footerRow = exportSheet.newRow();

		int pos = 0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			pos++;
		}

		footerRow.addCell(pos, translator.translate("export.footer.lectures.hint"));
	}

	private void addHeaders_1(OpenXMLWorksheet exportSheet) {
		Row headerRow = exportSheet.newRow();
		int pos = 0;
		
		Formatter formatter = Formatter.getInstance(translator.getLocale());
		String[] args = new String[] {
			lectureBlock.getTitle(),
			formatter.formatDate(lectureBlock.getStartDate()),
			formatter.formatTimeShort(lectureBlock.getStartDate()),
			formatter.formatTimeShort(lectureBlock.getEndDate())
		};
		headerRow.addCell(pos, translator.translate("export.header.lectureblocks", args));
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			pos++;
		}
		
		if(teachers != null && !teachers.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for(Identity teacher:teachers) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(userManager.getUserDisplayName(teacher));
			}
			
			headerRow.addCell(pos, translator.translate("export.header.teachers", new String[]{ sb.toString() }));
		}
		
		if(StringHelper.containsNonWhitespace(lectureBlock.getLocation())) {
			pos += lectureBlock.getPlannedLecturesNumber();
			headerRow.addCell(pos, translator.translate("export.header.location", new String[]{ lectureBlock.getLocation() }));
		}
	}

	private void addHeaders_2(OpenXMLWorksheet exportSheet) {
		Row headerRow = exportSheet.newRow();
		int pos = 0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			pos++;
		}
		headerRow.addCell(pos++, translator.translate("export.header.lectures"));
	}
	
	private void addHeaders_3(OpenXMLWorksheet exportSheet) {
		Row headerRow = exportSheet.newRow();
		
		int pos = 0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			headerRow.addCell(pos++, translator.translate("form.name." + propName));
		}
		
		for(int i=0; i<lectureBlock.getPlannedLecturesNumber(); i++) {
			headerRow.addCell(pos++, Integer.toString(i + 1));	
		}
		
		if(authorizedAbsenceEnabled) {
			//authorized absence
			headerRow.addCell(pos++, translator.translate("table.header.authorized.absence"));
			//authorized absence reason
			headerRow.addCell(pos++, translator.translate("authorized.absence.reason"));
		}
		
		//comment
		headerRow.addCell(pos++, translator.translate("table.header.comment"));
	}
	
	private void addContent(OpenXMLWorksheet exportSheet) {
		List<AbsenceNotice> notices = lectureService.getAbsenceNoticeRelatedTo(lectureBlock);

		List<Identity> participants = lectureService.getParticipants(lectureBlock);
		List<LectureBlockRollCall> rollCalls = lectureService.getRollCalls(lectureBlock);
		Map<Identity,LectureBlockRollCall> participantToRollCallMap = rollCalls.stream()
				.collect(Collectors.toMap(LectureBlockRollCall::getIdentity, Function.identity()));
		
		for(Identity participant:participants) {
			Row row = exportSheet.newRow();
			
			int pos = 0;
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				if (userPropertyHandler == null) continue;
				String val = userPropertyHandler.getUserProperty(participant.getUser(), translator.getLocale());
				row.addCell(pos++, val);
			}
			
			AbsenceNotice notice = notices.stream()
					.filter(n -> n.getIdentity().equals(participant))
					.findFirst().orElse(null);
			
			LectureBlockRollCall rollCall = participantToRollCallMap.get(participant);
			if(rollCall != null) {
				addRollCallContent(rollCall, notice, row, pos);
			} else if(notice != null) {
				addNoticeContent(notice, row, pos);
			}
		}
	}
	
	private void addRollCallContent(LectureBlockRollCall rollCall, AbsenceNotice notice, Row row, int pos) {
		List<Integer> absentList = rollCall.getLecturesAbsentList();
		for(int i=0; i<lectureBlock.getPlannedLecturesNumber(); i++) {
			String val = absentList.contains(i) || notice != null ? "x" : null;
			row.addCell(pos++, val);
		}
		
		boolean authorized = (rollCall.getAbsenceAuthorized() != null && rollCall.getAbsenceAuthorized().booleanValue())
				|| (notice != null && notice.getAbsenceAuthorized() != null && notice.getAbsenceAuthorized());

		if(authorizedAbsenceEnabled && authorized) {
			row.addCell(pos++, "x");
			
			StringBuilder sb = new StringBuilder();
			if(StringHelper.containsNonWhitespace(rollCall.getAbsenceReason())) {
				sb.append(rollCall.getAbsenceReason());
			}
			if(notice != null && StringHelper.containsNonWhitespace(notice.getAbsenceReason())) {
				if(sb.length() > 0) sb.append(" ");
				sb.append(notice.getAbsenceReason());
			}
			row.addCell(pos, sb.toString());
		}
	}
	
	private void addNoticeContent(AbsenceNotice notice, Row row, int pos) {
		for(int i=0; i<lectureBlock.getPlannedLecturesNumber(); i++) {
			row.addCell(pos++, "x");
		}
		
		boolean authorized = (notice != null && notice.getAbsenceAuthorized() != null && notice.getAbsenceAuthorized());
		if(authorizedAbsenceEnabled && authorized) {
			row.addCell(pos++, "x");
			row.addCell(pos, notice.getAbsenceReason());
		}
	}
}
