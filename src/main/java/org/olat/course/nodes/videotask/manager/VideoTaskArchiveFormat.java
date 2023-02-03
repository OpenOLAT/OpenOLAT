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
package org.olat.course.nodes.videotask.manager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.openxml.workbookstyle.CellStyle;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.course.nodes.videotask.model.VideoTaskArchiveSearchParams;
import org.olat.course.nodes.videotask.ui.VideoTaskEditController;
import org.olat.course.nodes.videotask.ui.VideoTaskHelper;
import org.olat.course.nodes.videotask.ui.VideoTaskParticipantListController;
import org.olat.ims.qti21.manager.archive.QTI21ArchiveFormat;
import org.olat.ims.qti21.ui.QTI21RuntimeController;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.VideoTaskSegmentSelection;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.model.VideoTaskCategoryScore;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskArchiveFormat {
	
	private static final Logger log = Tracing.createLoggerFor(VideoTaskArchiveFormat.class);
	public static final String TEST_USER_PROPERTIES = QTI21ArchiveFormat.TEST_USER_PROPERTIES;
	
	private Translator translator;
	private VideoSegments videoSegments;
	private final List<String> categoriesIds;
	private final List<VideoSegmentCategory> categories;
	private final VideoTaskArchiveSearchParams searchParams;
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private VideoAssessmentService videoAssessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public VideoTaskArchiveFormat(Locale locale, VideoTaskArchiveSearchParams searchParams) {
		CoreSpringFactory.autowireObject(this);
		
		this.searchParams = searchParams;
		videoSegments = videoManager.loadSegments(searchParams.getVideoEntry().getOlatResource());

		categoriesIds = searchParams.getCourseNode().getModuleConfiguration()
				.getList(VideoTaskEditController.CONFIG_KEY_CATEGORIES, String.class);
		categories = VideoTaskHelper.getSelectedCategories(videoSegments, categoriesIds);
		VideoTaskHelper.sortCategories(categories, searchParams.getCourseNode(), locale);
		
		translator = Util.createPackageTranslator(VideoTaskParticipantListController.class, locale);
		translator = Util.createPackageTranslator(QTI21RuntimeController.class, locale, translator);
		translator = userManager.getPropertyHandlerTranslator(translator);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(TEST_USER_PROPERTIES, true);
	}
	
	public MediaResource exportCourseElement() {
		VideoTaskCourseNode courseNode = searchParams.getCourseNode();
		String label = courseNode.getType() + "_"
				+ StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";

		//content
		return new OpenXMLWorkbookResource(label) {
			@Override
			protected void generate(OutputStream out) {
				final List<VideoTaskSession> sessions = videoAssessmentService
						.getTaskSessions(searchParams.getEntry(), searchParams.getCourseNode().getIdent());		
				try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
					//headers
					OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
					exportSheet.setHeaderRows(1);
					writeHeaders(exportSheet, workbook);
					writeData(sessions, exportSheet, workbook);
				} catch (Exception e) {
					log.error("", e);
				}
			}
		};
	}
	
	public void exportCourseElement(ZipOutputStream exportStream, VideoTaskCourseNode courseNode, String currentPath) {
		String label = StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeWithMinutes(new Date())
				+ ".xlsx";
		String path = ZipUtil.concat(currentPath, label);
		
		try(OutputStream out = new ShieldOutputStream(exportStream)) {
			exportStream.putNextEntry(new ZipEntry(path));
			exportWorkbook(out);
			exportStream.closeEntry();
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	public void exportWorkbook(OutputStream exportStream) {
		//content
		final List<VideoTaskSession> sessions = videoAssessmentService
				.getTaskSessions(searchParams.getEntry(), searchParams.getCourseNode().getIdent());
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(exportStream, 1)) {
			//headers
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			writeHeaders(exportSheet, workbook);
			writeData(sessions, exportSheet, workbook);
		} catch(Exception e) {
			log.error("", e);
		}
	}
	
	private void writeHeaders(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();
		//first header
		Row headerRow = exportSheet.newRow();
		int col = 1;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) {
				continue;
			}
			String header = translator.translate(userPropertyHandler.i18nFormElementLabelKey());
			headerRow.addCell(col++, header, headerStyle);			
		}
		
		// add other user and session information
		headerRow.addCell(col++, translator.translate("column.header.homepage"), headerStyle);
		
		headerRow.addCell(col++, translator.translate("table.header.attempt"), headerStyle);

		// course node points and passed
		AssessmentConfig assessmentConfig = courseAssessmentService
				.getAssessmentConfig(searchParams.getEntry(), searchParams.getCourseNode());

		// course node points and passed
		if(Mode.none != assessmentConfig.getScoreMode()) {
			headerRow.addCell(col++, translator.translate("archive.table.header.node.points"), headerStyle);
		}
		if(Mode.none != assessmentConfig.getPassedMode()) {
			headerRow.addCell(col++, translator.translate("archive.table.header.node.passed"), headerStyle);
		}

		headerRow.addCell(col++, translator.translate("table.header.score.percent"), headerStyle);
		headerRow.addCell(col++, translator.translate("table.header.score.points"), headerStyle);
		headerRow.addCell(col++, translator.translate("column.header.passed"), headerStyle);
		headerRow.addCell(col++, translator.translate("column.header.date"), headerStyle);
		headerRow.addCell(col++, translator.translate("table.header.duration"), headerStyle);
		for(int i=0; i<categories.size(); i++) {
			VideoSegmentCategory category = categories.get(i);
			String categoryTitle = category.getLabelAndTitle();
			headerRow.addCell(col++, categoryTitle, headerStyle);
		}
	}
	
	/**
	 * The 2 lists, sessions and responses are order by the user name and the test session key.
	 * @param sessions A list of test sessions ordered by test session key
	 * @param responses A list of responses ordered by test session key
	 * @param exportSheet
	 * @param workbook
	 */
	private void writeData(List<VideoTaskSession> sessions, OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		int numOfSessions = sessions.size();
		for(int i=0; i<numOfSessions; i++) {
			VideoTaskSession taskSession = sessions.get(i);
			List<VideoTaskSegmentSelection> selections = videoAssessmentService.getTaskSegmentSelections(List.of(taskSession));
			writeDataRow(i + 1, taskSession, selections, exportSheet, workbook);	
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	private void writeDataRow(int num, VideoTaskSession taskSession, List<VideoTaskSegmentSelection> selections,
			OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		int col = 0;
		Row dataRow = exportSheet.newRow();
		dataRow.addCell(col++, num, null);//sequence number
		
		AssessmentEntry entry = taskSession.getAssessmentEntry();
		Identity assessedIdentity = entry.getIdentity();
		
		//user properties
		if(assessedIdentity == null) {
			for (UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
				if (userPropertyHandler != null) {
					if(userPropertyHandlers.get(0) == userPropertyHandler) {
						dataRow.addCell(col++, translator.translate("anonym.user"), null);
					} else {
						col++;
					}	
				}	
			}
		} else {
			User assessedUser = assessedIdentity.getUser();
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				if (userPropertyHandler != null) {
					String property = userPropertyHandler.getUserProperty(assessedUser, translator.getLocale());
					dataRow.addCell(col++, property, null);
				}
			}
		}
		
		//homepage
		String homepage;
		if(entry.getIdentity() == null) {
			homepage = "";
		} else {
			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(entry.getIdentity());
			homepage = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);
		}
		dataRow.addCell(col++, homepage, null);
		
		// Attempt
		dataRow.addCell(col++, taskSession.getAttempt(), null);
		
		// course node points and passed
		AssessmentConfig assessmentConfig = courseAssessmentService
				.getAssessmentConfig(searchParams.getEntry(), searchParams.getCourseNode());
		if(Mode.none != assessmentConfig.getScoreMode()) {
			if(entry.getScore() != null) {
				dataRow.addCell(col++, entry.getScore(), null);
			} else {
				col++;
			}
		}
		if(Mode.none != assessmentConfig.getPassedMode()) {
			if(entry.getPassed() != null) {
				dataRow.addCell(col++, entry.getPassed().toString(), null);
			} else {
				col++;
			}
		}
		
		// Score in percent, in points, passed
		if(taskSession.getResult() != null) {
			dataRow.addCell(col, taskSession.getResultInPercent(), null);
		}
		col++;
		
		if(entry.getScore() != null) {
			dataRow.addCell(col, entry.getScore(), null);
		} 
		col++;
		
		if(entry.getPassed() != null) {
			dataRow.addCell(col, entry.getPassed().toString(), null);
		} 
		col++;
		
		// Date
		if(taskSession.getFinishTime() != null) {
			dataRow.addCell(col, taskSession.getFinishTime(), workbook.getStyles().getDateTimeStyle());
		}
		col++;
		
		// Duration
		Long duration = null;
		if(taskSession.getFinishTime() != null) {
			duration = taskSession.getDuration();
		}
		dataRow.addCell(col++, toDurationInMinutes(duration), null);
		
		// Categories
		VideoTaskCategoryScore[] categoriesScoring = videoAssessmentService.calculateScorePerCategory(categories, selections);
		for(VideoTaskCategoryScore categoryScoring:categoriesScoring) {
			int correct = categoryScoring.correct();
			dataRow.addCell(col++, correct, null);
		}
	}
	
	private Long toDurationInMinutes(Long valueMilliSeconds) {
		if(valueMilliSeconds == null || valueMilliSeconds.longValue() == 0) return null;
		return valueMilliSeconds.longValue() / (1000l * 60l);
	}

}
