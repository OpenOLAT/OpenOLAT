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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.openxml.workbookstyle.CellStyle;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.QTIExportFormatConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti.export.QTIArchiver;
import org.olat.ims.qti.export.QTIExportFormatter;
import org.olat.ims.qti.export.QTIExportItemFormatConfig;
import org.olat.ims.qti.export.helper.IdentityAnonymizerCallback;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.AssessmentResponseDAO;
import org.olat.ims.qti21.manager.QTI21ServiceImpl;
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
import org.olat.ims.qti21.ui.QTI21RuntimeController;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

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
	
	private static final OLog log = Tracing.createLoggerFor(QTI21ArchiveFormat.class);
	
	private Translator translator;
	
	private ResolvedAssessmentTest resolvedAssessmentTest;
	private List<UserPropertyHandler> userPropertyHandlers;
	private IdentityAnonymizerCallback anonymizerCallback;

	private final QTI21StatisticSearchParams searchParams;
	private QTIExportItemFormatConfig exportConfig;
	
	private List<ItemInfos> itemInfos;
	private final Map<String, InteractionArchive> interactionArchiveMap = new HashMap<>();
	
	private final QTI21Service qtiService;
	private final UserManager userManager;
	private final AssessmentResponseDAO responseDao;
	
	public QTI21ArchiveFormat(Locale locale, QTI21StatisticSearchParams searchParams) {
		this.searchParams = searchParams;
		if(searchParams.getArchiveOptions() == null || searchParams.getArchiveOptions().getQtiExportItemFormatConfig() == null) {
			exportConfig = new QTIExportFormatConfig(true, true, true, true);
		} else {
			exportConfig = searchParams.getArchiveOptions().getQtiExportItemFormatConfig();
		}
		
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		qtiService = CoreSpringFactory.getImpl(QTI21ServiceImpl.class);
		responseDao = CoreSpringFactory.getImpl(AssessmentResponseDAO.class);
		
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(QTIArchiver.TEST_USER_PROPERTIES, true);
		
		translator = Util.createPackageTranslator(QTI21RuntimeController.class, locale);
		translator = Util.createPackageTranslator(QTIExportFormatter.class, locale, translator);
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
	public void exportCourseElement(ZipOutputStream exportStream) {
		ICourse course = CourseFactory.loadCourse(searchParams.getCourseEntry());
		CourseNode courseNode = course.getRunStructure().getNode(searchParams.getNodeIdent());
		String label = StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeWithMinutes(new Date())
				+ ".xlsx";
		
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
	
	private void export(String filename,  ZipOutputStream exportStream) {
		try {
			exportStream.putNextEntry(new ZipEntry(filename));
			exportWorkbook(new ShieldOutputStream(exportStream));
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
		
		//content
		List<AssessmentResponse> responses = responseDao.getResponse(searchParams);
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(exportStream, 1)) {
			//headers
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			exportSheet.setHeaderRows(2);
			writeHeaders_1(exportSheet, workbook);
			writeHeaders_2(exportSheet, workbook);
			writeData(responses, exportSheet, workbook);
		} catch(Exception e) {
			log.error("", e);
		}
	}
	
	public MediaResource exportCourseElement() {
		FileResourceManager frm = FileResourceManager.getInstance();
		File unzippedDirRoot = frm.unzipFileResource(searchParams.getTestEntry().getOlatResource());
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
		
		ICourse course = CourseFactory.loadCourse(searchParams.getCourseEntry());
		CourseNode courseNode = course.getRunStructure().getNode(searchParams.getNodeIdent());
		String label = courseNode.getType() + "_"
				+ StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
		
		if("iqself".equals(courseNode.getType())) {
			anonymizerCallback = course.getCourseEnvironment().getCoursePropertyManager();
		}
		
		//content
		final List<AssessmentResponse> responses = responseDao.getResponse(searchParams);
		return new OpenXMLWorkbookResource(label) {
			@Override
			protected void generate(OutputStream out) {
				try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
					//headers
					OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
					exportSheet.setHeaderRows(2);
					writeHeaders_1(exportSheet, workbook);
					writeHeaders_2(exportSheet, workbook);
					writeData(responses, exportSheet, workbook);
				} catch (Exception e) {
					log.error("", e);
				}
			}
		};
	}
	
	
	private void writeHeaders_1(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		//first header
		Row header1Row = exportSheet.newRow();
		int col = 1;
		if(anonymizerCallback != null) {
			col += 4;// anonymized name -> test duration
		} else {
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				if (userPropertyHandler != null) {
					col++;
				}
			}
			col += 5;// homepage -> test duration
		}
		
		List<ItemInfos> infos = getItemInfos();
		for(int i=0; i<infos.size(); i++) {
			int delta = col;
			ItemInfos item = infos.get(i);
			if (exportConfig.hasResponseCols() || exportConfig.hasPointCol() || exportConfig.hasTimeCols()) {
				List<Interaction> interactions = item.getInteractions();
				for(int j=0; j<interactions.size(); j++) {
					Interaction interaction = interactions.get(j);
					col = interactionArchiveMap.get(interaction.getQtiClassName())
							.writeHeader1(item.getAssessmentItem(), interaction, i, j, header1Row, col, workbook);
				}
			}
			if (!exportConfig.hasResponseCols()) {
				col -= col - delta;
			}
			if (exportConfig.hasPointCol()) {
				col++;
			}
			if (exportConfig.hasTimeCols()) {
				col += anonymizerCallback != null ? 1 : 2;
			}		
		}
	}

	private void writeHeaders_2(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();
		//second header
		int col = 0;//reset column counter
		Row header2Row = exportSheet.newRow();
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
		
		header2Row.addCell(col++, translator.translate("column.header.assesspoints"), headerStyle);
		header2Row.addCell(col++, translator.translate("column.header.passed"), headerStyle);
		if (anonymizerCallback == null){
			header2Row.addCell(col++, translator.translate("column.header.date"), headerStyle);
		}
		header2Row.addCell(col++, translator.translate("column.header.duration"), headerStyle);

		List<ItemInfos> infos = getItemInfos();
		for(int i=0; i<infos.size(); i++) {
			ItemInfos info = infos.get(i);
			if (exportConfig.hasResponseCols()) {
				List<Interaction> interactions = info.getInteractions();
				for(int j=0; j<interactions.size(); j++) {
					Interaction interaction = interactions.get(j);
					col = interactionArchiveMap.get(interaction.getQtiClassName())
							.writeHeader2(info.getAssessmentItem(), interaction, i, j, header2Row, col, workbook);
				}
			}
			if (exportConfig.hasPointCol()) {
				header2Row.addCell(col++, translator.translate("item.score"), headerStyle);
			}
			if (exportConfig.hasTimeCols()) {
				if (anonymizerCallback == null){
					header2Row.addCell(col++, translator.translate("item.start"), headerStyle);
				}
				header2Row.addCell(col++, translator.translate("item.duration"), headerStyle);
			}
		}
	}
	
	private void writeData(List<AssessmentResponse> responses, OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		int num = 0;
		SessionResponses sessionResponses = null;
		int numOfResponses = responses.size();
		for(int i=0; i<numOfResponses; i++) {
			AssessmentResponse response = responses.get(i);
			AssessmentItemSession itemSession = response.getAssessmentItemSession();
			AssessmentTestSession testSession = itemSession.getAssessmentTestSession();
			
			if(sessionResponses == null) {
				sessionResponses = new SessionResponses(testSession);
			} else if(!sessionResponses.getSessionKey().equals(testSession.getKey())) {
				writeDataRow(++num, sessionResponses, exportSheet, workbook);
				sessionResponses = new SessionResponses(testSession);
			}
			sessionResponses.addResponse(itemSession, response);
		}
		if(sessionResponses != null) {
			writeDataRow(++num, sessionResponses, exportSheet, workbook);
		}
	}
	
	private void writeDataRow(int num, SessionResponses responses, OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		int col = 0;
		Row dataRow = exportSheet.newRow();
		dataRow.addCell(col++, num, null);//sequence number
		
		AssessmentTestSession testSession = responses.getTestSession();
		AssessmentEntry entry = testSession.getAssessmentEntry();
		
		//user properties
		if(entry.getIdentity() == null) {
			for (UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
				if (userPropertyHandler != null) {
					if(userPropertyHandlers.get(0) == userPropertyHandler) {
						dataRow.addCell(col++, translator.translate("anonym.user"), null);
					} else {
						col++;
					}	
				}	
			}
		} else if(anonymizerCallback != null) {
			String anonymizedName = anonymizerCallback.getAnonymizedUserName(entry.getIdentity());
			dataRow.addCell(col++, anonymizedName, null);
		} else {
			User assessedUser = entry.getIdentity().getUser();
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
		
		//assesspoints, passed, ipaddress, date, duration
		if(testSession.getScore() != null) {
			dataRow.addCell(col++, testSession.getScore(), null);
		} else {
			col++;
		}
		if(testSession.getPassed() != null) {
			dataRow.addCell(col++, testSession.getPassed().toString(), null);
		} else {
			col++;
		}
		if (anonymizerCallback == null){
			dataRow.addCell(col++, testSession.getCreationDate(), workbook.getStyles().getDateStyle());
		}
		dataRow.addCell(col++, toDurationInMilliseconds(testSession.getDuration()), null);

		List<ItemInfos> infos = getItemInfos();
		for(int i=0; i<infos.size(); i++) {
			ItemInfos info = infos.get(i);
			AssessmentItemRef itemRef = info.getAssessmentItemRef();
			String itemRefIdentifier = itemRef.getIdentifier().toString();
			AssessmentItemSession itemSession = responses.getItemSession(itemRefIdentifier);
			
			if (exportConfig.hasResponseCols()) {
				List<Interaction> interactions = info.getInteractions();
				for(int j=0; j<interactions.size(); j++) {
					Interaction interaction = interactions.get(j);
					 AssessmentResponse response = responses
							 .getResponse(itemRefIdentifier, interaction.getResponseIdentifier());
					col = interactionArchiveMap.get(interaction.getQtiClassName())
								.writeInteractionData(info.getAssessmentItem(), response, interaction, j, dataRow, col, workbook);
				}
			}
			
			//score, start, duration
			if (itemSession == null) {
				if (exportConfig.hasPointCol()) {
					col++;
				}
				if (exportConfig.hasTimeCols()) {
					col += anonymizerCallback != null ? 1 : 2;
				}
			} else {
				if (exportConfig.hasPointCol()) {
					dataRow.addCell(col++, itemSession.getScore(), null);
				}
				if (exportConfig.hasTimeCols()) {
					if (anonymizerCallback == null){
						dataRow.addCell(col++, itemSession.getCreationDate(), workbook.getStyles().getTimeStyle());
					}
					dataRow.addCell(col++, toDurationInMilliseconds(itemSession.getDuration()), null);
				}
			}
		}
	}
	
	private Long toDurationInMilliseconds(Long value) {
		if(value == null || value.longValue() == 0) return null;
		return value.longValue() / 1000l;
	}
	
	private List<ItemInfos> getItemInfos() {
		if(itemInfos == null) {
			itemInfos = new ArrayList<>();
			List<AssessmentItemRef> itemRefs = resolvedAssessmentTest.getAssessmentItemRefs();
			for(AssessmentItemRef itemRef:itemRefs) {
				ResolvedAssessmentItem resolvedItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
				AssessmentItem item = resolvedItem.getRootNodeLookup().extractIfSuccessful();
				if(item != null) {
					itemInfos.add(new ItemInfos(itemRef, item, item.getItemBody().findInteractions()));
				}
			}
		}
		return itemInfos;
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
		
		public Long getSessionKey() {
			return testSession.getKey();
		}
		
		public AssessmentResponse getResponse(String itemRefIdentifier, Identifier responseIdentifier) {
			if(responseIdentifier == null) return null;
			String responseIdentifierStr = responseIdentifier.toString();
			List<AssessmentResponse> itemResponses = responsesMap.get(itemRefIdentifier);
			
			AssessmentResponse response = null;
			if(itemResponses != null && itemResponses.size() > 0) {
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
			if(!itemSessionsMap.containsKey(itemIdentifier)) {
				itemSessionsMap.put(itemIdentifier, itemSession);
				responsesMap.put(itemIdentifier, new ArrayList<>(5));
			}
			responsesMap.get(itemIdentifier).add(response);	
		}
		
		public AssessmentItemSession getItemSession(String itemIdentifier) {
			return itemSessionsMap.get(itemIdentifier);
		}
	}
	
	private static class ItemInfos {
		
		private final AssessmentItemRef itemRef;
		private final AssessmentItem assessmentItem;
		private final List<Interaction> interactions;
		
		public ItemInfos(AssessmentItemRef itemRef, AssessmentItem assessmentItem, List<Interaction> interactions) {
			this.itemRef = itemRef;
			this.interactions = interactions;
			this.assessmentItem = assessmentItem;
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
	}
}
