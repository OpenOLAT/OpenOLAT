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
package org.olat.course.nodes.cl.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.openxml.workbookstyle.CellStyle;
import org.olat.course.CourseEntryRef;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.course.nodes.cl.model.AssessmentData;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.model.DBCheck;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * This export all the data and it doesn't filter the datas
 * based on their roles.
 * 
 * Initial date: 19 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListExcelExport {
	
	private static final Logger log = Tracing.createLoggerFor(CheckListExcelExport.class);

	private Translator translator;
	private final ICourse course;
	private final CheckListCourseNode courseNode;
	private final boolean hasScore;
	private final boolean hasPassed;
	private final boolean hasComment;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private final UserManager userManager;
	private final CheckboxManager checkboxManager;
	
	public CheckListExcelExport(CheckListCourseNode courseNode, ICourse course, Locale locale) {
		this.courseNode = courseNode;
		this.course = course;
		
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(course), courseNode);
		this.hasScore = Mode.none != assessmentConfig.getScoreMode();
		this.hasPassed = Mode.none != assessmentConfig.getPassedMode();
		this.hasComment = assessmentConfig.hasComment();
		
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(CheckListAssessmentController.USER_PROPS_ID, true);
		
		translator = Util.createPackageTranslator(CheckListAssessmentController.class, locale);
		translator = userManager.getPropertyHandlerTranslator(translator);
	}
	
	public void exportAll(String filename, ZipOutputStream exportStream) {
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<AssessmentData> dataList = checkboxManager.getAssessmentDatas(course, courseNode.getIdent(), courseEntry, null, true);
		try(OutputStream out = new ShieldOutputStream(exportStream)) {
			exportStream.putNextEntry(new ZipEntry(filename + ".xlsx"));
			exportWorkbook(dataList, out);
			exportStream.closeEntry();
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	public void exportWorkbook(List<AssessmentData> dataList, OutputStream exportStream) {
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(exportStream, 1)) {
			//headers
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			exportSheet.setHeaderRows(1);
			writeHeaders(exportSheet, workbook);
			writeData(dataList, exportSheet);
		} catch(Exception e) {
			log.error("", e);
		}
	}

	private void writeHeaders(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();
		//second header
		int col = 0;//reset column counter
		Row header2Row = exportSheet.newRow();
		String sequentialNumber = translator.translate("column.header.seqnum");
		header2Row.addCell(col++, sequentialNumber, headerStyle);

		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) {
				continue;
			}
			String header = translator.translate(userPropertyHandler.i18nFormElementLabelKey());
			header2Row.addCell(col++, header, headerStyle);			
		}
		
		// add other user and session information
		header2Row.addCell(col++, translator.translate("column.header.homepage"), headerStyle);

		// course node points and passed
		if(hasScore) {
			header2Row.addCell(col++, translator.translate("column.header.node.points"), headerStyle);
		}
		if(hasPassed) {
			header2Row.addCell(col++, translator.translate("column.header.node.passed"), headerStyle);
		}
		if(hasComment) {
			header2Row.addCell(col++, translator.translate("column.header.usercomment"), headerStyle);
		}

		ModuleConfiguration config = courseNode.getModuleConfiguration();
		CheckboxList list = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		if(list != null && list.getList() != null) {
			List<Checkbox> checkboxList = list.getList();
			for(Checkbox checkbox:checkboxList) {
				String boxTitle = checkbox.getTitle();
				header2Row.addCell(col++, boxTitle, headerStyle);
				if(hasScore && checkbox.getPoints() != null) {
					header2Row.addCell(col++, translator.translate("column.header.points"), headerStyle);
				}	
			}
		}
	}

	private void writeData(List<AssessmentData> dataList, OpenXMLWorksheet exportSheet) {
		List<AssessmentEntry> entries = course.getCourseEnvironment().getAssessmentManager().getAssessmentEntries(courseNode);
		Map<Identity,AssessmentEntry> entryMap = new HashMap<>();
		for(AssessmentEntry entry:entries) {
			entryMap.put(entry.getIdentity(), entry);
		}

		int num = 1;
		for(AssessmentData data:dataList) {
			AssessmentEntry entry = entryMap.get(data.getIdentity());
			writeDataRow(data, entry, num++, exportSheet);
		}
	}
	
	private void writeDataRow(AssessmentData data, AssessmentEntry entry, int num, OpenXMLWorksheet exportSheet) {
		int col = 0;
		Identity assessedIdentity = data.getIdentity();
		User assessedUser = assessedIdentity.getUser();
		
		Row dataRow = exportSheet.newRow();
		dataRow.addCell(col++, num, null);//sequence number
		
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler != null) {
				String property = userPropertyHandler.getUserProperty(assessedUser, translator.getLocale());
				dataRow.addCell(col++, property, null);
			}
		}
		
		//homepage
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(assessedIdentity);
		String homepage = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);
		dataRow.addCell(col++, homepage, null);
		
		// course node points and passed
		if(hasScore) {
			if(entry != null && entry.getScore() != null) {
				dataRow.addCell(col++, entry.getScore(), null);
			} else {
				col++;
			}
		}
		if(hasPassed) {
			if(entry != null && entry.getPassed() != null) {
				dataRow.addCell(col++, entry.getPassed().toString(), null);
			} else {
				col++;
			}
		}

		if(hasComment) {
			if(entry != null) {
				dataRow.addCell(col++, entry.getComment(), null);
			} else {
				col++;
			}
		}
		
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		CheckboxList list = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		if(list != null && list.getList() != null && data.getChecks() != null) {
			Map<String,DBCheck> checkMap = data.getChecks()
					.stream().collect(Collectors.toMap(c -> c.getCheckbox().getCheckboxId(), c -> c));
			List<Checkbox> checkboxList = list.getList();
			for(Checkbox checkbox:checkboxList) {
				String boxId = checkbox.getCheckboxId();
				DBCheck check = checkMap.get(boxId);
				if(check != null && check.getChecked() != null && check.getChecked().booleanValue()) {
					dataRow.addCell(col++, "x", null);
				} else {
					col++;
				}
				
				if(hasScore && checkbox.getPoints() != null) {
					if(check != null && check.getScore() != null ) {
						dataRow.addCell(col++, check.getScore(), null);
					} else {
						col++;
					}
				}
			}
		}
	}
}
