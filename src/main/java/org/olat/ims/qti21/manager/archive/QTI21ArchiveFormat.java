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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.openxml.workbookstyle.CellStyle;
import org.olat.course.CourseFactory;
import org.olat.course.nodes.CourseNode;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti.export.QTIArchiver;
import org.olat.ims.qti.export.QTIExportFormatter;
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
import org.olat.ims.qti21.manager.archive.interactions.HotspotInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.InteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.MatchInteractionArchive;
import org.olat.ims.qti21.manager.archive.interactions.TextEntryInteractionArchive;
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
	private final Map<String,ItemInfos> itemInfosMap = new HashMap<>();
	private final Map<String, InteractionArchive> interactionArchiveMap = new HashMap<>();
	
	private final QTI21Service qtiService;
	private final UserManager userManager;
	private final AssessmentResponseDAO responseDao;
	
	public QTI21ArchiveFormat(Locale locale) {
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		qtiService = CoreSpringFactory.getImpl(QTI21ServiceImpl.class);
		responseDao = CoreSpringFactory.getImpl(AssessmentResponseDAO.class);
		
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(QTIArchiver.TEST_USER_PROPERTIES, true);
		
		translator = Util.createPackageTranslator(QTI21ArchiveFormat.class, locale);
		translator = Util.createPackageTranslator(QTIExportFormatter.class, locale, translator);
		translator = userManager.getPropertyHandlerTranslator(translator);
		initInteractionWriters();
	}
	
	private void initInteractionWriters() {
		interactionArchiveMap.put(AssociateInteraction.QTI_CLASS_NAME, new AssociateInteractionArchive());
		interactionArchiveMap.put(ChoiceInteraction.QTI_CLASS_NAME, new ChoiceInteractionArchive());
		interactionArchiveMap.put(DrawingInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
		interactionArchiveMap.put(ExtendedTextInteraction.QTI_CLASS_NAME, new ExtendedTextInteractionArchive());
		interactionArchiveMap.put(GapMatchInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
		interactionArchiveMap.put(GraphicAssociateInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
		interactionArchiveMap.put(GraphicGapMatchInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
		interactionArchiveMap.put(GraphicOrderInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive()); 
		interactionArchiveMap.put(HotspotInteraction.QTI_CLASS_NAME, new HotspotInteractionArchive());
		interactionArchiveMap.put(SelectPointInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
		interactionArchiveMap.put(HottextInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
		interactionArchiveMap.put(MatchInteraction.QTI_CLASS_NAME, new MatchInteractionArchive());
		interactionArchiveMap.put(MediaInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
		interactionArchiveMap.put(OrderInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
		interactionArchiveMap.put(PositionObjectInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
		interactionArchiveMap.put(SliderInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
		interactionArchiveMap.put(UploadInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
	//custom
		interactionArchiveMap.put(CustomInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
	//inline
		interactionArchiveMap.put(EndAttemptInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
		interactionArchiveMap.put(InlineChoiceInteraction.QTI_CLASS_NAME, new DefaultInteractionArchive());
		interactionArchiveMap.put(TextEntryInteraction.QTI_CLASS_NAME, new TextEntryInteractionArchive());
	}
	
	public boolean hasResults(RepositoryEntry courseEntry, String subIdent, RepositoryEntry testEntry) {
		return responseDao.hasResponses(courseEntry, subIdent, testEntry);
	}
	
	public MediaResource export(RepositoryEntry courseEntry, String subIdent, RepositoryEntry testEntry) {
		
		FileResourceManager frm = FileResourceManager.getInstance();
		File unzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false);
		
		CourseNode courseNode = CourseFactory.loadCourse(courseEntry).getRunStructure().getNode(subIdent);
		String label = courseNode.getType() + "_"
				+ StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
		
		//content
		final List<AssessmentResponse> responses = responseDao.getResponse(courseEntry, subIdent, testEntry);

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
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler != null) {
				col++;
			}
		}
		col += 5;// homepage -> test duration
		
		List<AssessmentItemRef> itemRefs = getAssessmentItemRefs();
		for(int i=0; i<itemRefs.size(); i++) {
			AssessmentItemRef itemRef = itemRefs.get(i);
			ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
			AssessmentItem item = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
			
			ItemInfos itemInfos = getItemInfos(itemRef);
			List<Interaction> interactions = itemInfos.getInteractions();
			for(int j=0; j<interactions.size(); j++) {
				Interaction interaction = interactions.get(j);
				col = interactionArchiveMap.get(interaction.getQtiClassName())
						.writeHeader1(item, interaction, i, j, header1Row, col, workbook);
			}
			col += 3;//score, start, duration
		}
	}

	private void writeHeaders_2(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
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
		header2Row.addCell(col++, translator.translate("column.header.assesspoints"), headerStyle);
		header2Row.addCell(col++, translator.translate("column.header.passed"), headerStyle);
		//header2Row.addCell(col++, translator.translate("column.header.ipaddress"), headerStyle);
		header2Row.addCell(col++, translator.translate("column.header.date"), headerStyle);
		header2Row.addCell(col++, translator.translate("column.header.duration"), headerStyle);
		
		List<AssessmentItemRef> itemRefs = getAssessmentItemRefs();
		for(int i=0; i<itemRefs.size(); i++) {
			AssessmentItemRef itemRef = itemRefs.get(i);
			ItemInfos itemInfos = getItemInfos(itemRef);
			List<Interaction> interactions = itemInfos.getInteractions();
			for(int j=0; j<interactions.size(); j++) {
				Interaction interaction = interactions.get(j);
				col = interactionArchiveMap.get(interaction.getQtiClassName())
						.writeHeader2(interaction, i, j, header2Row, col, workbook);
			}

			header2Row.addCell(col++, translator.translate("item.score"), headerStyle);
			header2Row.addCell(col++, translator.translate("item.start"), headerStyle);
			header2Row.addCell(col++, translator.translate("item.duration"), headerStyle);
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
		writeDataRow(++num, sessionResponses, exportSheet, workbook);
	}
	
	private void writeDataRow(int num, SessionResponses responses, OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		int col = 0;
		Row dataRow = exportSheet.newRow();
		dataRow.addCell(col++, num, null);//sequence number
		
		AssessmentTestSession testSession = responses.getTestSession();
		AssessmentEntry entry = testSession.getAssessmentEntry();
		
		//user properties
		User assessedUser = entry.getIdentity().getUser();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) {
				continue;
			}
			String property = userPropertyHandler.getUserProperty(assessedUser, translator.getLocale());
			dataRow.addCell(col++, property, null);
		}
		
		//homepage, assesspoints, passed, ipaddress, date, duration
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(entry.getIdentity());
		String homepage = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);
		dataRow.addCell(col++, homepage, null);
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
		//dataRow.addCell(col++, "0.0.0.1", null);
		dataRow.addCell(col++, testSession.getCreationDate(), workbook.getStyles().getDateStyle());
		dataRow.addCell(col++, toDurationInMilliseconds(testSession.getDuration()), null);
		
		List<AssessmentItemRef> itemRefs = getAssessmentItemRefs();
		for(int i=0; i<itemRefs.size(); i++) {
			AssessmentItemRef itemRef = itemRefs.get(i);
			String itemRefIdentifier = itemRef.getIdentifier().toString();
			AssessmentItemSession itemSession = responses.getItemSession(itemRefIdentifier);
			ItemInfos itemInfos = getItemInfos(itemRef);
			AssessmentItem item = itemInfos.getAssessmentItem();

			List<Interaction> interactions = itemInfos.getInteractions();
			for(int j=0; j<interactions.size(); j++) {
				Interaction interaction = interactions.get(j);
				 AssessmentResponse response = responses
						 .getResponse(itemRefIdentifier, interaction.getResponseIdentifier());
				col = interactionArchiveMap.get(interaction.getQtiClassName())
							.writeInteractionData(item, response, interaction, j, dataRow, col, workbook);
			}
			
			//score, start, duration
			if(itemSession == null) {
				col += 3;
			} else {
				dataRow.addCell(col++, itemSession.getScore(), null);
				dataRow.addCell(col++, itemSession.getCreationDate(), workbook.getStyles().getTimeStyle());
				dataRow.addCell(col++, toDurationInMilliseconds(itemSession.getDuration()), null);
			}
		}
	}
	
	private Long toDurationInMilliseconds(Long value) {
		if(value == null || value.longValue() == 0) return null;
		return value.longValue() / 1000l;
	}
	
	private List<AssessmentItemRef> getAssessmentItemRefs() {
		return resolvedAssessmentTest.getAssessmentItemRefs();
	}
	
	private ItemInfos getItemInfos(AssessmentItemRef itemRef) {
		String itemRefIdentifier = itemRef.getIdentifier().toString();
		ItemInfos itemInfos = itemInfosMap.get(itemRefIdentifier);
		if(itemInfos == null) {
			ResolvedAssessmentItem resolvedItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
			AssessmentItem item = resolvedItem.getRootNodeLookup().extractIfSuccessful();
			itemInfos = new ItemInfos(item, item.getItemBody().findInteractions());
			itemInfosMap.put(itemRefIdentifier, itemInfos);
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
		
		private final AssessmentItem assessmentItem;
		private final List<Interaction> interactions;
		
		public ItemInfos(AssessmentItem assessmentItem, List<Interaction> interactions) {
			this.interactions = interactions;
			this.assessmentItem = assessmentItem;
		}
		
		public AssessmentItem getAssessmentItem() {
			return assessmentItem;
		}

		public List<Interaction> getInteractions() {
			return interactions;
		}
	}
}
