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
package org.olat.ims.qti21.manager.archive;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
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
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.openxml.workbookstyle.CellStyle;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.ExportFormat;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.properties.IdentityAnonymizerCallback;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.AssessmentItemSessionDAO;
import org.olat.ims.qti21.manager.AssessmentResponseDAO;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.ims.qti21.manager.archive.interactions.AssociateInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.ChoiceInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.DefaultInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.ExtendedTextInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.GapMatchInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.GraphicAssociateInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.GraphicGapMatchInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.GraphicOrderInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.HotspotInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.HottextInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.InlineChoiceInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.InteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.MatchInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.MediaInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.NoOutputInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.OrderInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.PositionObjectInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.SelectPointInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.SliderInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.TextEntryInteractionArchive;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.ui.QTI21RuntimeController;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.qpool.ui.QuestionItemDetailsController;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.AssociateInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.CustomInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.DrawingInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GapMatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicAssociateInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicGapMatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicOrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HottextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.InlineChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MediaInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.OrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.PositionObjectInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.SelectPointInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.SliderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.SectionPart;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 22.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ArchiveFormat {
	
	private static final Logger log = Tracing.createLoggerFor(QTI21ArchiveFormat.class);
	

	public static final String TEST_USER_PROPERTIES = "org.olat.ims.qti.export.QTIExportFormatterCSVType1";
	
	private Translator translator;
	
	private ResolvedAssessmentTest resolvedAssessmentTest;
	private List<UserPropertyHandler> userPropertyHandlers;
	private IdentityAnonymizerCallback anonymizerCallback;

	private final QTI21StatisticSearchParams searchParams;
	private ExportFormat exportConfig;
	
	private int numOfSections;
	private CourseNode courseNode;
	private ManifestBuilder manifestBuilder;
	private List<AbstractInfos> elementInfos;
	private final Map<String, InteractionArchive> interactionArchiveMap = new HashMap<>();
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AssessmentResponseDAO responseDao;
	@Autowired
	private AssessmentTestSessionDAO testSessionDao;
	@Autowired
	private AssessmentItemSessionDAO itemSessionDao;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public QTI21ArchiveFormat(Locale locale, QTI21StatisticSearchParams searchParams) {
		CoreSpringFactory.autowireObject(this);
		
		this.searchParams = searchParams;
		if(searchParams.getArchiveOptions() == null || searchParams.getArchiveOptions().getExportFormat() == null) {
			exportConfig = new ExportFormat(true, true, true, true, true);
		} else {
			exportConfig = searchParams.getArchiveOptions().getExportFormat();
		}
		
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(TEST_USER_PROPERTIES, true);
		
		translator = Util.createPackageTranslator(QTI21RuntimeController.class, locale,
				Util.createPackageTranslator(QuestionItemDetailsController.class, locale));
		translator = userManager.getPropertyHandlerTranslator(translator);
		initInteractionWriters();
	}
	
	private void initInteractionWriters() {
		interactionArchiveMap.put(AssociateInteraction.QTI_CLASS_NAME, new AssociateInteractionArchive());					//ok
		interactionArchiveMap.put(ChoiceInteraction.QTI_CLASS_NAME, new ChoiceInteractionArchive());						//ok
		interactionArchiveMap.put(DrawingInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());						//like file
		interactionArchiveMap.put(ExtendedTextInteraction.QTI_CLASS_NAME, new ExtendedTextInteractionArchive());			//ok
		interactionArchiveMap.put(GapMatchInteraction.QTI_CLASS_NAME, new GapMatchInteractionArchive());					//ok
		interactionArchiveMap.put(GraphicAssociateInteraction.QTI_CLASS_NAME, new GraphicAssociateInteractionArchive());	//ok
		interactionArchiveMap.put(GraphicGapMatchInteraction.QTI_CLASS_NAME, new GraphicGapMatchInteractionArchive());		//ok
		interactionArchiveMap.put(GraphicOrderInteraction.QTI_CLASS_NAME, new GraphicOrderInteractionArchive()); 			//ok
		interactionArchiveMap.put(HotspotInteraction.QTI_CLASS_NAME, new HotspotInteractionArchive());						//ok
		interactionArchiveMap.put(SelectPointInteraction.QTI_CLASS_NAME, new SelectPointInteractionArchive());				//ok
		interactionArchiveMap.put(HottextInteraction.QTI_CLASS_NAME, new HottextInteractionArchive());						//ok
		interactionArchiveMap.put(MatchInteraction.QTI_CLASS_NAME, new MatchInteractionArchive());							//ok
		interactionArchiveMap.put(MediaInteraction.QTI_CLASS_NAME, new MediaInteractionArchive());
		interactionArchiveMap.put(OrderInteraction.QTI_CLASS_NAME, new OrderInteractionArchive());							//ok
		interactionArchiveMap.put(PositionObjectInteraction.QTI_CLASS_NAME, new PositionObjectInteractionArchive());
		interactionArchiveMap.put(SliderInteraction.QTI_CLASS_NAME, new SliderInteractionArchive());						//ok
		interactionArchiveMap.put(UploadInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
	//custom
		interactionArchiveMap.put(CustomInteraction.QTI_CLASS_NAME, new NoOutputInteractionArchive());						//ok
	//inline
		interactionArchiveMap.put(EndAttemptInteraction.QTI_CLASS_NAME, new NoOutputInteractionArchive());					//ok
		interactionArchiveMap.put(InlineChoiceInteraction.QTI_CLASS_NAME, new InlineChoiceInteractionArchive());			//ok
		interactionArchiveMap.put(TextEntryInteraction.QTI_CLASS_NAME, new TextEntryInteractionArchive());					//ok
	}
	
	public boolean hasResults() {
		return responseDao.hasResponses(searchParams);
	}

	/**
	 * 
	 * @param exportStream
	 */
	public void exportCourseElement(ZipOutputStream exportStream, String currentPath) {
		ICourse course = CourseFactory.loadCourse(searchParams.getCourseEntry());
		courseNode = course.getRunStructure().getNode(searchParams.getNodeIdent());
		String label = StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeWithMinutes(new Date())
				+ ".xlsx";
		String path = ZipUtil.concat(currentPath, label);
		exportCourseElement(path, exportStream);
	}
	
	public void exportCourseElement(String label, ZipOutputStream exportStream) {
		ICourse course = CourseFactory.loadCourse(searchParams.getCourseEntry());
		courseNode = course.getRunStructure().getNode(searchParams.getNodeIdent());
		if("iqself".equals(courseNode.getType())) {
			anonymizerCallback = course.getCourseEnvironment().getCoursePropertyManager();
		}
		export(label, exportStream);
	}
	
	public void exportResource(ZipOutputStream exportStream) {
		String archiveName = "qti21test_"
				+ StringHelper.transformDisplayNameToFileSystemName(searchParams.getTestEntry().getDisplayname())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis())) + ".xlsx";
		export(archiveName, exportStream);
	}
	
	public void export(String filename,  ZipOutputStream exportStream) {
		try(OutputStream out = new ShieldOutputStream(exportStream)) {
			exportStream.putNextEntry(new ZipEntry(filename));
			exportWorkbook(out);
			exportStream.closeEntry();
		} catch (IOException e) {
			log.error("", e);
		}
	}
	

	
	public void exportWorkbook(OutputStream exportStream) {
		RepositoryEntry testEntry = searchParams.getTestEntry();
		FileResourceManager frm = FileResourceManager.getInstance();
		File unzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
		manifestBuilder = ManifestBuilder.read(new File(unzippedDirRoot, "imsmanifest.xml"));
		//content
		generateSheets(exportStream);
	}
	
	public MediaResource exportCourseElement() {
		FileResourceManager frm = FileResourceManager.getInstance();
		File unzippedDirRoot = frm.unzipFileResource(searchParams.getTestEntry().getOlatResource());
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
		
		ICourse course = CourseFactory.loadCourse(searchParams.getCourseEntry());
		courseNode = course.getRunStructure().getNode(searchParams.getNodeIdent());
		String label = courseNode.getType() + "_"
				+ StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
		
		if("iqself".equals(courseNode.getType())) {
			anonymizerCallback = course.getCourseEnvironment().getCoursePropertyManager();
		}
		
		//content
		return new OpenXMLWorkbookResource(label) {
			@Override
			protected void generate(OutputStream out) {	
				generateSheets(out);
			}
		};
	}
	
	private void generateSheets(OutputStream out) {
		final List<AssessmentTestSession> sessions = testSessionDao.getTestSessionsOfResponse(searchParams);
		final List<String> sheetsNames = List.of(translator.translate("export.sheet.results"),
				translator.translate("export.sheet.additional.infos"),
				translator.translate("export.sheet.coverage.results"));
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 3, sheetsNames)) {	
			//headers
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			exportSheet.setHeaderRows(2);
			writeHeaders_1(exportSheet, workbook);
			writeHeaders_2(exportSheet, workbook);
			writeData(sessions, exportSheet, workbook);
			
			// Write sheet 2: additional informations
			OpenXMLWorksheet additionalInfosSheet = workbook.nextWorksheet();
			additionalInfosSheet.setHeaderRows(2);
			writeAdditionalInfosHeaders(additionalInfosSheet, workbook);
			writeAdditionalInfosData(additionalInfosSheet);
			
			// Write sheet 3
			OpenXMLWorksheet coverageResultsSheet = workbook.nextWorksheet();
			coverageResultsSheet.setHeaderRows(3);
			writeCoverageResultsHeaders_1(coverageResultsSheet, workbook);
			writeCoverageResultsHeaders_2(coverageResultsSheet, workbook);
			writeCoverageResultsHeaders_3(coverageResultsSheet, workbook);
			writeCoverageResultsData(sessions, coverageResultsSheet);
		
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	
	private void writeHeaders_1(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();
		//first header
		Row header1Row = exportSheet.newRow();
		int col = writeUserEmptyHeaders();

		// course node points and passed
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(searchParams.getCourseEntry(), courseNode);
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
		if(hasScore) {
			header1Row.addCell(col++, translator.translate("archive.table.header.node"), headerStyle);
		}
		if(hasPassed) {
			if(hasScore) {
				col++;
			} else {
				header1Row.addCell(col++, translator.translate("archive.table.header.node"), headerStyle);
			}
		}

		// test points, passed and dates
		header1Row.addCell(col++, translator.translate("archive.table.header.test"), headerStyle);
		col += 7;
		
		List<AbstractInfos> infos = getItemInfos();
		for(int i=0; i<infos.size(); i++) {
			int delta = col;
			AbstractInfos info = infos.get(i);
			if(info instanceof ItemInfos item) {
				if (exportConfig.isResponseCols() || exportConfig.isPointCol() || exportConfig.isTimeCols() || exportConfig.isCommentCol()) {
					List<Interaction> interactions = item.getInteractions();
					for(int j=0; j<interactions.size(); j++) {
						Interaction interaction = interactions.get(j);
						col = interactionArchiveMap.get(interaction.getQtiClassName())
								.writeHeader1(item.getAssessmentItem(), interaction, i, j, header1Row, col, workbook);
					}
				}
				if (!exportConfig.isResponseCols()) {
					col -= col - delta;
				}
				if (exportConfig.isPointCol()) {
					col++;
				}
				if (exportConfig.isCommentCol()) {
					col++;
				}
				if (exportConfig.isTimeCols()) {
					col += anonymizerCallback != null ? 1 : 2;
				}
			} else if(numOfSections > 1 && info instanceof SectionInfos section) {
				if(!section.getItemInfos().isEmpty()) {
					String sectionTitle = translator.translate("archive.table.header.section", section.getAssessmentSection().getTitle());
					header1Row.addCell(col++, sectionTitle, headerStyle);
				}
			}
		}
	}
	
	private void writeHeaders_2(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();
		//second header
		Row header2Row = exportSheet.newRow();
		int col = writeUserHeaders(header2Row, workbook);

		// course node points and passed
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(searchParams.getCourseEntry(), courseNode);
		if(Mode.none != assessmentConfig.getScoreMode()) {
			header2Row.addCell(col++, translator.translate("archive.table.header.node.points"), headerStyle);
		}
		if(Mode.none != assessmentConfig.getPassedMode()) {
			header2Row.addCell(col++, translator.translate("archive.table.header.node.passed"), headerStyle);
		}
		
		header2Row.addCell(col++, translator.translate("archive.table.header.points"), headerStyle);
		header2Row.addCell(col++, translator.translate("archive.table.header.manual.points"), headerStyle);
		header2Row.addCell(col++, translator.translate("archive.table.header.final.points"), headerStyle);
		header2Row.addCell(col++, translator.translate("column.header.passed"), headerStyle);
		if (anonymizerCallback == null){
			header2Row.addCell(col++, translator.translate("column.header.date"), headerStyle);
		}
		header2Row.addCell(col++, translator.translate("column.header.duration"), headerStyle);
		header2Row.addCell(col++, translator.translate("archive.table.header.additional.time"), headerStyle);
		header2Row.addCell(col++, translator.translate("archive.table.header.compensation"), headerStyle);

		List<AbstractInfos> infos = getItemInfos();
		int itemPos = 0;
		for(int i=0; i<infos.size(); i++) {
			AbstractInfos info = infos.get(i);
			if(info instanceof ItemInfos) {
				ItemInfos item = (ItemInfos)info;
				if (exportConfig.isResponseCols()) {
					List<Interaction> interactions = item.getInteractions();
					for(int j=0; j<interactions.size(); j++) {
						Interaction interaction = interactions.get(j);
						col = interactionArchiveMap.get(interaction.getQtiClassName())
								.writeHeader2(item.getAssessmentItem(), interaction, itemPos, j, header2Row, col, workbook);
					}
					itemPos++;
				}
				if (exportConfig.isPointCol()) {
					header2Row.addCell(col++, translator.translate("item.score"), headerStyle);
				}
				if (exportConfig.isCommentCol()) {
					header2Row.addCell(col++, translator.translate("item.comment"), headerStyle);
				}
				if (exportConfig.isTimeCols()) {
					if (anonymizerCallback == null){
						header2Row.addCell(col++, translator.translate("item.start"), headerStyle);
					}
					header2Row.addCell(col++, translator.translate("item.duration"), headerStyle);
				}
			} else if(numOfSections > 1 && info instanceof SectionInfos section) {
				if(!section.getItemInfos().isEmpty()) {
					header2Row.addCell(col++, translator.translate("archive.table.header.points"), headerStyle);
				}
			}
		}
	}
	
	private int writeUserEmptyHeaders() {
		int col = 1; // Number column
		if(anonymizerCallback != null) {
			col += 0;// anonymized name -> test duration
		} else {
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				if (userPropertyHandler != null) {
					col++;
				}
			}
			col += 1;// homepage -> test duration
		}
		return col;
	}
	
	private int writeUserHeaders(Row header2Row, OpenXMLWorkbook workbook) {
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();
		//second header
		int col = 0;//reset column counter
		String sequentialNumber = translator.translate("column.header.seqnum");
		header2Row.addCell(col++, sequentialNumber, headerStyle);

		if(anonymizerCallback != null) {
			col++;
		} else {
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				if (userPropertyHandler == null) {
					continue;
				}
				String header = translator.translate(userPropertyHandler.i18nFormElementLabelKey());
				header2Row.addCell(col++, header, headerStyle);			
			}
			
			// add other user and session information
			header2Row.addCell(col++, translator.translate("column.header.homepage"), headerStyle);
		}
		return col;
	}
	
	/**
	 * The 2 lists, sessions and responses are order by the user name and the test session key.
	 * @param sessions A list of test sessions ordered by test session key
	 * @param responses A list of responses ordered by test session key
	 * @param exportSheet
	 * @param workbook
	 */
	private void writeData(List<AssessmentTestSession> sessions, OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		int numOfSessions = sessions.size();
		for(int i=0; i<numOfSessions; i++) {
			AssessmentTestSession testSession = sessions.get(i);
			SessionResponses sessionResponses = new SessionResponses(testSession);
			List<AssessmentResponse> responses = responseDao.getResponses(testSession);
			for(AssessmentResponse response:responses) {
				AssessmentItemSession itemSession = response.getAssessmentItemSession();
				sessionResponses.addResponse(itemSession, response);
			}
			
			List<AssessmentItemSession> itemSessions = itemSessionDao.getAssessmentItemSessions(testSession);
			for(AssessmentItemSession itemSession:itemSessions) {
				sessionResponses.addItemSession(itemSession);
			}
			
			writeDataRow(i + 1, sessionResponses, exportSheet, workbook);	
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	private void writeDataRow(int num, SessionResponses responses, OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		AssessmentTestSession testSession = responses.getTestSession();
		AssessmentEntry entry = testSession.getAssessmentEntry();
		Row dataRow = exportSheet.newRow();
		
		int col = writeUserData(num, entry, dataRow);
		
		// course node points and passed
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(searchParams.getCourseEntry(), courseNode);
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
		
		//assesspoints, passed, ipaddress, date, duration
		if(testSession.getScore() != null) {
			dataRow.addCell(col++, testSession.getScore(), null);
		} else {
			col++;
		}
		if(testSession.getManualScore() != null) {
			dataRow.addCell(col++, testSession.getManualScore(), null);
		} else {
			col++;
		}
		if(testSession.getFinalScore() != null) {
			dataRow.addCell(col++, testSession.getFinalScore(), null);
		} else {
			col++;
		}
		if(testSession.getPassed() != null) {
			dataRow.addCell(col++, testSession.getPassed().toString(), null);
		} else {
			col++;
		}
		if(anonymizerCallback == null) {
			dataRow.addCell(col++, testSession.getCreationDate(), workbook.getStyles().getDateTimeStyle());
		}
		dataRow.addCell(col++, toDurationInMilliseconds(testSession.getDuration()), null);
		
		if(testSession.getExtraTime() != null) {
			dataRow.addCell(col++, toDurationSecondsToMinutes(testSession.getExtraTime()), null);
		} else {
			col++;
		}
		
		if(testSession.getCompensationExtraTime() != null) {
			dataRow.addCell(col++, toDurationSecondsToMinutes(testSession.getCompensationExtraTime()), null);
		} else {
			col++;
		}
		
		List<AbstractInfos> infos = getItemInfos();
		for(int i=0; i<infos.size(); i++) {
			AbstractInfos info = infos.get(i);
			if(info instanceof ItemInfos item) {
				AssessmentItemRef itemRef = item.getAssessmentItemRef();
				String itemRefIdentifier = itemRef.getIdentifier().toString();
				AssessmentItemSession itemSession = responses.getItemSession(itemRefIdentifier);
				
				if (exportConfig.isResponseCols()) {
					List<Interaction> interactions = item.getInteractions();
					for(int j=0; j<interactions.size(); j++) {
						Interaction interaction = interactions.get(j);
						AssessmentResponse response = responses
								 .getResponse(itemRefIdentifier, interaction.getResponseIdentifier());
						col = interactionArchiveMap.get(interaction.getQtiClassName())
									.writeInteractionData(item.getAssessmentItem(), response, interaction, j, dataRow, col, workbook);
					}
				}
			
				//score, start, duration
				if (itemSession == null) {
					if (exportConfig.isPointCol()) {
						col++;
					}
					if (exportConfig.isCommentCol()) {
						col++;
					}
					if (exportConfig.isTimeCols()) {
						col += anonymizerCallback != null ? 1 : 2;
					}
				} else {
					if (exportConfig.isPointCol()) {
						if(itemSession.getManualScore() != null) {
							dataRow.addCell(col++, itemSession.getManualScore(), null);
						} else {
							dataRow.addCell(col++, itemSession.getScore(), null);
						}
					}
					if (exportConfig.isCommentCol()) {
						dataRow.addCell(col++, getCoachComment(itemSession), null);	
					}
					if (exportConfig.isTimeCols()) {
						if (anonymizerCallback == null){
							dataRow.addCell(col++, itemSession.getCreationDate(), workbook.getStyles().getDateStyle());
						}
						dataRow.addCell(col++, toDurationInMilliseconds(itemSession.getDuration()), null);
					}
				}
			} else if(numOfSections > 1 && info instanceof SectionInfos section) {
				if(!section.getItemInfos().isEmpty()) {
					BigDecimal score = calculateSectionScore(responses, section);
					if(score != null) {
						dataRow.addCell(col++, score, workbook.getStyles().getLightGrayStyle());
					} else {
						col++;
					}
				}
			}
		}
	}
	
	private int writeUserData(int num, AssessmentEntry entry, Row dataRow) {
		Identity assessedIdentity = entry.getIdentity();
		
		int col = 0;
		dataRow.addCell(col++, num, null);//sequence number

		//user properties
		if(anonymizerCallback != null) {
			String anonymizedName;
			if(assessedIdentity == null) {
				anonymizedName = translator.translate("anonym.user");
			} else {
				anonymizedName = anonymizerCallback.getAnonymizedUserName(assessedIdentity);
			}
			dataRow.addCell(col++, anonymizedName, null);
		} else if(assessedIdentity == null) {
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
		if(anonymizerCallback == null) {
			String homepage;
			if(entry.getIdentity() == null) {
				homepage = "";
			} else {
				ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(entry.getIdentity());
				homepage = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);
			}
			dataRow.addCell(col++, homepage, null);
		}
		
		return col;
	}
	
	private void writeAdditionalInfosHeaders(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();
		Row header1Row = exportSheet.newRow();
		header1Row.addCell(0, translator.translate("export.additional.infos"), headerStyle);

		Row header2Row = exportSheet.newRow();
		header2Row.addCell(0, translator.translate("column.header.question.id"), headerStyle);
		header2Row.addCell(1, translator.translate("general.coverage"), headerStyle);
		header2Row.addCell(2, translator.translate("classification.taxonomic.path"), headerStyle);
	}
	
	private void writeAdditionalInfosData(OpenXMLWorksheet exportSheet) {
		List<AbstractInfos> infos = getItemInfos();
		for(AbstractInfos info:infos) {
			if(info instanceof ItemInfos item) {
				Row row = exportSheet.newRow();
				String identifier = item.getAssessmentItem().getIdentifier();
				row.addCell(0, identifier);
				
				ManifestMetadataBuilder metadata = item.getMetadata();
				if(metadata != null) {
					List<String> coverageList = metadata.getCoverageList();
					String coverage = null;
					if(coverageList != null && !coverageList.isEmpty()) {
						coverage = Strings.join(coverageList, ';');
					}
					row.addCell(1, coverage);
					String taxonomyPath = metadata.getClassificationTaxonomy();
					row.addCell(2, taxonomyPath);
				}
			}
		}
	}
	
	private void writeCoverageResultsHeaders_1(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();
		Row header1Row = exportSheet.newRow();
		int col = writeUserEmptyHeaders();
		
		List<AbstractInfos> infos = getItemInfos();
		for(AbstractInfos info:infos) {
			if(info instanceof ItemInfos item) {
				String identifier = item.getAssessmentItem().getIdentifier();
				header1Row.addCell(col++, identifier, headerStyle);
				
				ManifestMetadataBuilder metadata = item.getMetadata();
				int coverageSize = metadata == null ? 0 : metadata.getCoverageList().size();
				coverageSize -= 1;
				for(int i=0; i<coverageSize; i++) {
					col++;
				}
			}
		}
	}
	
	private void writeCoverageResultsHeaders_2(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();
		Row header2Row = exportSheet.newRow();
		int col = writeUserEmptyHeaders();
		
		List<AbstractInfos> infos = getItemInfos();
		for(AbstractInfos info:infos) {
			if(info instanceof ItemInfos item) {
				ManifestMetadataBuilder metadata = item.getMetadata();
				List<String> coverageList = metadata == null ? null: metadata.getCoverageList();
				if(coverageList == null || coverageList.isEmpty()) {
					col++;
				} else {
					for(String coverage:coverageList) {
						header2Row.addCell(col++, coverage, headerStyle);
					}
				}
			}
		}
	}
	
	private void writeCoverageResultsHeaders_3(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();
		Row header2Row = exportSheet.newRow();
		int col = writeUserHeaders(header2Row, workbook);
		
		List<AbstractInfos> infos = getItemInfos();
		for(AbstractInfos info:infos) {
			if(info instanceof ItemInfos item) {
				ManifestMetadataBuilder metadata = item.getMetadata();
				int loop = metadata == null ? 0 : metadata.getCoverageList().size();
				if(loop <= 0) {
					loop = 1;
				}
				for(int i=0; i<loop; i++) {
					header2Row.addCell(col++, translator.translate("item.score"), headerStyle);
				}
			}
		}	
	}
	
	private void writeCoverageResultsData(List<AssessmentTestSession> sessions, OpenXMLWorksheet exportSheet) {
		int numOfSessions = sessions.size();
		for(int i=0; i<numOfSessions; i++) {
			AssessmentTestSession testSession = sessions.get(i);
			SessionResponses sessionResponses = new SessionResponses(testSession);
			List<AssessmentResponse> responses = responseDao.getResponses(testSession);
			for(AssessmentResponse response:responses) {
				AssessmentItemSession itemSession = response.getAssessmentItemSession();
				sessionResponses.addResponse(itemSession, response);
			}
			
			List<AssessmentItemSession> itemSessions = itemSessionDao.getAssessmentItemSessions(testSession);
			for(AssessmentItemSession itemSession:itemSessions) {
				sessionResponses.addItemSession(itemSession);
			}
			
			writeCoverageResultsDataRow(i + 1, sessionResponses, exportSheet);	
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	private void writeCoverageResultsDataRow(int num, SessionResponses responses, OpenXMLWorksheet exportSheet) {
		AssessmentTestSession testSession = responses.getTestSession();
		AssessmentEntry entry = testSession.getAssessmentEntry();
		Row dataRow = exportSheet.newRow();

		int col = writeUserData(num, entry, dataRow);

		List<AbstractInfos> infos = getItemInfos();
		for(int i=0; i<infos.size(); i++) {
			AbstractInfos info = infos.get(i);
			if(info instanceof ItemInfos item) {
				AssessmentItemRef itemRef = item.getAssessmentItemRef();
				String itemRefIdentifier = itemRef.getIdentifier().toString();
				AssessmentItemSession itemSession = responses.getItemSession(itemRefIdentifier);
				//score, start, duration

				ManifestMetadataBuilder metadata = item.getMetadata();
				int loop = metadata == null ? 0 : metadata.getCoverageList().size();
				if(loop <= 0) {
					loop = 1;
				}
				
				for(int j=0; j<loop; j++) {
					if (itemSession == null) {
						if (exportConfig.isPointCol()) {
							col++;
						}
					} else {
						if (exportConfig.isPointCol()) {
							if(itemSession.getManualScore() != null) {
								dataRow.addCell(col++, itemSession.getManualScore(), null);
							} else {
								dataRow.addCell(col++, itemSession.getScore(), null);
							}
						}
					}
				}
			}
		}
	}
	
	private String getCoachComment(AssessmentItemSession itemSession) {
		if(itemSession == null || !StringHelper.containsNonWhitespace(itemSession.getCoachComment())) return null;
		
		String comment = itemSession.getCoachComment();
		if(StringHelper.isHtml(comment)) {
			comment = FilterFactory.getHtmlTagAndDescapingFilter().filter(comment);
		}
		return comment;
	}
	
	private BigDecimal calculateSectionScore(SessionResponses responses, SectionInfos section) {
		BigDecimal sectionScore = BigDecimal.valueOf(0l);
		
		for(ItemInfos item:section.getItemInfos()) {
			AssessmentItemRef itemRef = item.getAssessmentItemRef();
			String itemRefIdentifier = itemRef.getIdentifier().toString();
			AssessmentItemSession itemSession = responses.getItemSession(itemRefIdentifier);
			if(itemSession != null) {
				if(itemSession.getManualScore() != null) {
					sectionScore = sectionScore.add(itemSession.getManualScore());
				} else if(itemSession.getScore() != null){
					sectionScore = sectionScore.add(itemSession.getScore());
				}
			}
		}
		
		return sectionScore;
	}
	
	private Integer toDurationSecondsToMinutes(Integer value) {
		if(value == null || value.intValue() == 0) return null;
		return Math.round(value.floatValue() / 60.0f);
	}
	
	private Long toDurationInMilliseconds(Long value) {
		if(value == null || value.longValue() == 0) return null;
		return value.longValue() / 1000l;
	}
	
	private List<AbstractInfos> getItemInfos() {
		if(elementInfos == null) {
			numOfSections = 0;
			elementInfos = new ArrayList<>();
			
			AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractAssumingSuccessful();
			for(TestPart part:assessmentTest.getTestParts()) {
				for(AssessmentSection section:part.getAssessmentSections()) {
					collectElementInfos(section);
				}
			}
		}
		return elementInfos;
	}
	
	private void collectElementInfos(AssessmentSection section) {
		numOfSections++;
		SectionInfos sectionInfos = new SectionInfos(section);
		elementInfos.add(sectionInfos);

		List<SectionPart> parts = section.getChildAbstractParts();
		for(SectionPart part:parts) {
			if(part instanceof AssessmentItemRef itemRef) {
				ResolvedAssessmentItem resolvedItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
				AssessmentItem item = resolvedItem.getRootNodeLookup().extractIfSuccessful();
				ManifestMetadataBuilder metadata = manifestBuilder.getResourceBuilderByHref(itemRef.getHref().toString());
				if(item != null) {
					ItemInfos itemInfo = new ItemInfos(itemRef, item, item.getItemBody().findInteractions(), metadata);
					elementInfos.add(itemInfo);
					sectionInfos.getItemInfos().add(itemInfo);
				}
			} else if(part instanceof AssessmentSection subSection) {
				collectElementInfos(subSection);
			}
		}
	}
	
	private static class SessionResponses {
		
		private final AssessmentTestSession testSession;
		private final Map<String,AssessmentItemSession> itemSessionsMap = new HashMap<>();
		private final Map<String,List<AssessmentResponse>> responsesMap = new HashMap<>();
		
		public SessionResponses(AssessmentTestSession testSession) {
			this.testSession = testSession;
		}
		
		public AssessmentTestSession getTestSession() {
			return testSession;
		}
		
		public AssessmentResponse getResponse(String itemRefIdentifier, Identifier responseIdentifier) {
			if(responseIdentifier == null) return null;
			String responseIdentifierStr = responseIdentifier.toString();
			List<AssessmentResponse> itemResponses = responsesMap.get(itemRefIdentifier);
			
			AssessmentResponse response = null;
			if(itemResponses != null && !itemResponses.isEmpty()) {
				for(AssessmentResponse itemResponse:itemResponses) {
					if(responseIdentifierStr.equals(itemResponse.getResponseIdentifier())) {
						response = itemResponse;
						break;
					}
				}
			}
			return response;
		}
		
		public void addResponse(AssessmentItemSession itemSession, AssessmentResponse response) {
			String itemIdentifier = itemSession.getAssessmentItemIdentifier();
			itemSessionsMap.putIfAbsent(itemIdentifier, itemSession);
			responsesMap.computeIfAbsent(itemIdentifier, id -> new ArrayList<>(5))
				.add(response);	
		}
		
		public void addItemSession(AssessmentItemSession itemSession) {
			itemSessionsMap.put(itemSession.getAssessmentItemIdentifier(), itemSession);
		}
		
		public AssessmentItemSession getItemSession(String itemIdentifier) {
			return itemSessionsMap.get(itemIdentifier);
		}
	}
	
	private static class AbstractInfos {
		//
	}
	
	private static class SectionInfos extends AbstractInfos {
		private final AssessmentSection section;
		private final List<ItemInfos> itemInfos = new ArrayList<>();
		
		public SectionInfos(AssessmentSection section) {
			this.section = section;
		}
		
		public AssessmentSection getAssessmentSection() {
			return section;
		}
		
		public List<ItemInfos> getItemInfos() {
			return itemInfos;
		}
	}
	
	private static class ItemInfos extends AbstractInfos {
		
		private final AssessmentItemRef itemRef;
		private final AssessmentItem assessmentItem;
		private final List<Interaction> interactions;

		private final ManifestMetadataBuilder metadata;
		
		public ItemInfos(AssessmentItemRef itemRef, AssessmentItem assessmentItem, List<Interaction> interactions,
				ManifestMetadataBuilder metadata) {
			this.itemRef = itemRef;
			this.interactions = interactions;
			this.assessmentItem = assessmentItem;
			this.metadata = metadata;
		}
		
		public AssessmentItemRef getAssessmentItemRef() {
			return itemRef;
		}
		
		public AssessmentItem getAssessmentItem() {
			return assessmentItem;
		}

		public List<Interaction> getInteractions() {
			return interactions;
		}
		
		public ManifestMetadataBuilder getMetadata() {
			return metadata;
		}
	}
}
