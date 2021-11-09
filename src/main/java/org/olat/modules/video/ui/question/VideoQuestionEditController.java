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
package org.olat.modules.video.ui.question;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.pool.QTI21QPoolServiceProvider;
import org.olat.ims.qti21.ui.assessment.components.QuestionTypeFlexiCellRenderer;
import org.olat.ims.qti21.ui.editor.AssessmentItemEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.ui.SelectItemController;
import org.olat.modules.qpool.ui.events.QItemViewEvent;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.modules.video.model.VideoQuestionImpl;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoDisplayController.Marker;
import org.olat.modules.video.ui.VideoDisplayOptions;
import org.olat.modules.video.ui.VideoHelper;
import org.olat.modules.video.ui.VideoSettingsController;
import org.olat.modules.video.ui.component.SelectTimeCommand;
import org.olat.modules.video.ui.component.VideoMarkerStyleCellRenderer;
import org.olat.modules.video.ui.component.VideoTimeCellRenderer;
import org.olat.modules.video.ui.event.VideoEvent;
import org.olat.modules.video.ui.marker.ReloadMarkersCommand;
import org.olat.modules.video.ui.question.VideoQuestionsTableModel.QuestionCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;

/**
 * 
 * Initial date: 4 déc. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoQuestionEditController extends BasicController {
	
	private Panel editorWrapper = new Panel("editor");
	
	private VideoDisplayController videoDisplayCtrl;
	private VideoQuestionsController questionsCtrl;
	private AssessmentItemEditorController itemEditorCtrl;
	private QuestionConfigurationController questionConfigCtrl;

	private String currentTimeCode;
	private long durationInSeconds;
	private String videoElementId;
	private final RepositoryEntry entry;
	
	public VideoQuestionEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, Util.createPackageTranslator(VideoSettingsController.class, ureq.getLocale()));
		this.entry = entry;
	
		VelocityContainer mainVC = createVelocityContainer("questions_overview");
		VideoDisplayOptions displayOptions = VideoDisplayOptions.disabled();
		displayOptions.setShowQuestions(true);
		displayOptions.setShowAnnotations(false);
		displayOptions.setAlwaysShowControls(true);
		displayOptions.setShowPoster(false);
		displayOptions.setClickToPlayPause(false);
		displayOptions.setAuthorMode(true);
		videoDisplayCtrl = new VideoDisplayController(ureq, getWindowControl(), entry, null, null, displayOptions);
		videoElementId = videoDisplayCtrl.getVideoElementId();
		durationInSeconds = VideoHelper.durationInSeconds(entry, videoDisplayCtrl);
		listenTo(videoDisplayCtrl);
		mainVC.put("video", videoDisplayCtrl.getInitialComponent());
		
		questionsCtrl = new VideoQuestionsController(ureq, getWindowControl());
		listenTo(questionsCtrl);
		mainVC.put("questions", questionsCtrl.getInitialComponent());
		mainVC.put("editor", editorWrapper);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (videoDisplayCtrl == source) {
			if (event instanceof VideoEvent) {
				VideoEvent videoEvent = (VideoEvent) event;
				currentTimeCode = videoEvent.getTimeCode();
				if(StringHelper.containsNonWhitespace(videoEvent.getDuration()) && !"NaN".equals(videoEvent.getDuration())) {
					try {
						durationInSeconds = Math.round(Double.parseDouble(videoEvent.getDuration()));
					} catch (NumberFormatException e) {
						//don't panic
					}
				}
				
				if(questionConfigCtrl != null) {
					questionConfigCtrl.setVideoDurationInSecs(durationInSeconds);
				}
			}
		} else if(questionConfigCtrl == source) {
			questionsCtrl.event(ureq, questionConfigCtrl, event);
		}
		super.event(ureq, source, event);
	}
	
	private void selectTime(Date time) {
		long timeInSeconds = time.getTime() / 1000l;
		SelectTimeCommand selectTime = new SelectTimeCommand(videoElementId, timeInSeconds);
		getWindowControl().getWindowBackOffice().sendCommandTo(selectTime);
	}
	
	private void loadMarker(UserRequest ureq, VideoQuestion question) {
		if(question == null) return;
		
		String time = String.valueOf(question.toSeconds());
		videoDisplayCtrl.loadMarker(ureq, time, question.getId());
	}
	
	private void reloadMarkers() {
		List<Marker> markers = videoDisplayCtrl.loadMarkers();
		ReloadMarkersCommand reloadMarkers = new ReloadMarkersCommand(videoElementId, markers);
		getWindowControl().getWindowBackOffice().sendCommandTo(reloadMarkers);
	}

	public class VideoQuestionsController extends FormBasicController {

		private FormLink newQuestionButton;
		private FormLink importQuestionButton;
		private FlexiTableElement tableEl;
		private VideoQuestionsTableModel tableModel;

		private VideoQuestions questions;
		private CloseableModalController cmc;
		private SelectItemController selectQItemCtrl;
		private NewQuestionItemCalloutController newQuestionCtrl;
		private CloseableCalloutWindowController newQuestionCalloutCtrl;
		
		@Autowired
		private QTI21Service qtiService;
		@Autowired
		private VideoManager videoManager;
		@Autowired
		private QPoolService questionPoolService;
		@Autowired
		private QTI21QPoolServiceProvider qti21QPoolServiceProvider;
		
		public VideoQuestionsController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "questions_list", Util.createPackageTranslator(VideoSettingsController.class,
					 ureq.getLocale(), Util.createPackageTranslator(AssessmentItemEditorController.class, ureq.getLocale())));
			
			initForm(ureq);
			loadModel(true);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionCols.start, "select", new VideoTimeCellRenderer()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionCols.type, new QuestionTypeFlexiCellRenderer(getTranslator())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionCols.title));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionCols.score, new ScoreCellRenderer()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionCols.style, new VideoMarkerStyleCellRenderer(getTranslator())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
			tableModel = new VideoQuestionsTableModel(columnsModel);

			tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
			tableEl.setCustomizeColumns(false);
			tableEl.setNumOfRowsEnabled(false);
			
			newQuestionButton = uifactory.addFormLink("video.question.new", formLayout, Link.BUTTON);
			newQuestionButton.setIconLeftCSS("o_icon o_icon_qitem_new");
			importQuestionButton = uifactory.addFormLink("video.question.import", formLayout, Link.BUTTON);
			importQuestionButton.setIconLeftCSS("o_icon o_icon_qitem_import");
		}
		
		private void loadModel(boolean reset) {
			questions = videoManager.loadQuestions(entry.getOlatResource());
			List<VideoQuestion> rows = questions.getQuestions();
			loadModel(reset, rows);
		}
		
		private void loadModel(boolean reset, List<VideoQuestion> rows) {
			if(rows.size() > 1) {
				Collections.sort(rows, new VideoQuestionRowComparator());
			}
			tableModel.setObjects(rows);
			tableEl.reset(reset, reset, true);
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
			//
		}

		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if(itemEditorCtrl == source) {
				if(event instanceof AssessmentItemEvent) {
					doSaveQuestion(ureq, questionConfigCtrl.getQuestion());
				}
			} else if(questionConfigCtrl == source) {
				if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
					doSaveQuestion(ureq, questionConfigCtrl.getQuestion());
				}
			} else if(newQuestionCtrl == source) {
				newQuestionCalloutCtrl.deactivate();
				cleanUp();
				if(event instanceof NewQuestionEvent) {
					NewQuestionEvent nqe = (NewQuestionEvent)event;
					doNewQuestion(ureq, nqe.getQuestion());
				}
			} else if(selectQItemCtrl == source) {
				if(event instanceof QItemViewEvent && "select-item".equals(event.getCommand())) {
					doCopyQItems(ureq, ((QItemViewEvent)event).getItemList());
				}
				cmc.deactivate();
				cleanUp();
			} else if(newQuestionCalloutCtrl == source || cmc == source) {
				cleanUp();
			}
			super.event(ureq, source, event);
		}
		
		private void cleanUp() {
			removeAsListenerAndDispose(newQuestionCalloutCtrl);
			removeAsListenerAndDispose(questionConfigCtrl);
			removeAsListenerAndDispose(newQuestionCtrl);
			removeAsListenerAndDispose(itemEditorCtrl);
			removeAsListenerAndDispose(selectQItemCtrl);
			newQuestionCalloutCtrl = null;
			questionConfigCtrl = null;
			selectQItemCtrl = null;
			newQuestionCtrl = null;
			itemEditorCtrl = null;
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(newQuestionButton == source) {
				doNewQuestion(ureq);
			} else if(importQuestionButton == source) {
				doSelectQItem(ureq);
			} else if(tableEl == source) {
				if(event instanceof SelectionEvent) {
					SelectionEvent se = (SelectionEvent)event;
					if("edit".equals(se.getCommand())) {
						VideoQuestion row = tableModel.getObject(se.getIndex());
						doEditQuestion(ureq, row);
					} else if("delete".equals(se.getCommand())) {
						VideoQuestion row = tableModel.getObject(se.getIndex());
						doDeleteQuestion(row);
					} else if("select".equals(se.getCommand())) {
						VideoQuestion row = tableModel.getObject(se.getIndex());
						doSelectQuestion(ureq, row);
					}
				}
			}
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
		
		private void doSelectQuestion(UserRequest ureq, VideoQuestion question) {
			selectTime(question.getBegin());
			doEditQuestion(ureq, question);
		}
		
		private void doSelectQItem(UserRequest ureq) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(selectQItemCtrl);
			
			List<QItemType> itemTypes = questionPoolService.getAllItemTypes();
			List<QItemType> excludedItemTypes = new ArrayList<>();
			for(QItemType t:itemTypes) {
				if(t.getType().equalsIgnoreCase(QuestionType.DRAWING.name())
						|| t.getType().equalsIgnoreCase(QuestionType.ESSAY.name())
						|| t.getType().equalsIgnoreCase(QuestionType.UPLOAD.name())) {
					excludedItemTypes.add(t);
				}
			}

			selectQItemCtrl = new SelectItemController(ureq, getWindowControl(), QTI21Constants.QTI_21_FORMAT, excludedItemTypes);
			listenTo(selectQItemCtrl);

			cmc = new CloseableModalController(getWindowControl(), translate("close"), selectQItemCtrl.getInitialComponent(), true, translate("title.add") );
			cmc.activate();
			listenTo(cmc);
		}
		
		private void doCopyQItems(UserRequest ureq, List<QuestionItemView> items) {
			File assessmentDir = videoManager.getAssessmentDirectory(entry.getOlatResource());
			long currentTime = getCurrentTime();
			
			VideoQuestion firstQuestion = null;
			for(QuestionItemView item:items) {
				VideoQuestion importedQuestion = doCopyQItem(item, assessmentDir, currentTime);
				if(firstQuestion == null && importedQuestion != null) {
					firstQuestion = importedQuestion;
				}
				currentTime += 10l;
			}
			
			loadModel(true);
			if(firstQuestion != null) {
				doEditQuestion(ureq, firstQuestion);
			} else {
				reloadMarkers();
			}
		}
		
		private VideoQuestion doCopyQItem(QuestionItemView item, File assessmentDir, long begin) {
			try {
				QuestionItemFull qItem = qti21QPoolServiceProvider.getFullQuestionItem(item);
				
				String itemDir = buildItemDirectory(qItem);
				VideoQuestionImpl question = new VideoQuestionImpl();
				question.setId(itemDir);
				question.setBegin(new Date());
				question.setQuestionRootPath(itemDir);
				question.setQuestionFilename(qItem.getRootFilename());
				question.setBegin(new Date(begin));
				
				File itemDirectory = new File(assessmentDir, itemDir);
				itemDirectory.mkdir();
				AssessmentItem assessmentItem = qti21QPoolServiceProvider.exportToQTIEditor(qItem, getLocale(), itemDirectory);
				File itemFile = new File(itemDirectory, qItem.getRootFilename());
				qtiService.persistAssessmentObject(itemFile, assessmentItem);
				
				question.setTitle(assessmentItem.getTitle());
				question.setAssessmentItemIdentifier(assessmentItem.getIdentifier());
				question.setType(QTI21QuestionType.getTypeRelax(assessmentItem).name());
				Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
				question.setMaxScore(maxScore);
				questions.getQuestions().add(question);
				videoManager.saveQuestions(questions, entry.getOlatResource());	
				return question;
			} catch (IOException e) {
				logError("", e);
				return null;
			}
		}
		
		private String buildItemDirectory(QuestionItemFull qItem) {
			StringBuilder sb = new StringBuilder(48);
			if(qItem.getType() != null && StringHelper.containsNonWhitespace(qItem.getType().getType())) {
				sb.append(qItem.getType().getType());
			}  else {
				sb.append(QTI21QuestionType.unkown.name());
			}
			
			if(StringHelper.containsNonWhitespace(qItem.getIdentifier())) {
				sb.append(qItem.getIdentifier().replace("-", ""));
			} else {
				sb.append(UUID.randomUUID().toString().replace("-", ""));
			}
			return sb.toString();	
		}
		
		private void doNewQuestion(UserRequest ureq) {
			newQuestionCtrl = new NewQuestionItemCalloutController(ureq, getWindowControl(), entry);
			listenTo(newQuestionCtrl);
			
			newQuestionCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					newQuestionCtrl.getInitialComponent(), newQuestionButton.getFormDispatchId(),
					"", true, "", new CalloutSettings(false));
			listenTo(newQuestionCalloutCtrl);
			newQuestionCalloutCtrl.activate();
		}
		
		private void doNewQuestion(UserRequest ureq, VideoQuestion newQuestion) {
			newQuestion.setBegin(new Date(getCurrentTime()));
			
			questions.getQuestions().add(newQuestion);
			videoManager.saveQuestions(questions, entry.getOlatResource());
			loadModel(true);
			doEditQuestion(ureq, newQuestion);
		}
		
		private long getCurrentTime() {
			long time = 5l;
			if(currentTimeCode != null) {
				time = Math.round(Double.parseDouble(currentTimeCode)) * 1000l;
			}
			return time;
		}
		
		private void doSaveQuestion(UserRequest ureq, VideoQuestion question) {
			if(itemEditorCtrl != null && itemEditorCtrl.getAssessmentItem() != null) {
				AssessmentItem assessmentItem = itemEditorCtrl.getAssessmentItem();
				question.setTitle(assessmentItem.getTitle());
				question.setType(itemEditorCtrl.getType().name());
				question.setAssessmentItemIdentifier(assessmentItem.getIdentifier());
				Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
				question.setMaxScore(maxScore);
			}
			
			questions.getQuestions().remove(question);
			questions.getQuestions().add(question);
			videoManager.saveQuestions(questions, entry.getOlatResource());
			loadModel(false, questions.getQuestions());
			
			AssessmentTestSession candidateSession = videoDisplayCtrl.getCandidateSession();
			if(candidateSession != null) {
				qtiService.deleteAuthorAssessmentTestSession(entry, candidateSession);
			}
			
			loadMarkersAndQuestion(ureq, question);
		}
		
		private void doEditQuestion(UserRequest ureq, VideoQuestion question) {
			cleanUp();
			
			File rootDirectory = videoManager.getQuestionDirectory(entry.getOlatResource(), question);
			VFSContainer rootContainer = videoManager.getQuestionContainer(entry.getOlatResource(), question);
			File itemFile = new File(rootDirectory, question.getQuestionFilename());
			ResolvedAssessmentItem resolvedAssessmentItem = qtiService.loadAndResolveAssessmentItem(itemFile.toURI(), rootDirectory);
			itemEditorCtrl = new AssessmentItemEditorController(ureq, getWindowControl(),
					resolvedAssessmentItem, rootDirectory, rootContainer, itemFile, false, false);
			listenTo(itemEditorCtrl);
			
			questionConfigCtrl = new QuestionConfigurationController(ureq, getWindowControl(), question, durationInSeconds);
			listenTo(questionConfigCtrl);
			itemEditorCtrl.getTabbedPane().addTab(0, translate("video.question.configuration"), questionConfigCtrl);
			itemEditorCtrl.getTabbedPane().setSelectedPane(ureq, 0);
			editorWrapper.setContent(itemEditorCtrl.getInitialComponent());
			
			loadMarkersAndQuestion(ureq, question);
		}
		
		private void loadMarkersAndQuestion(UserRequest ureq, VideoQuestion question) {
			reloadMarkers();
			selectTime(question.getBegin());
			loadMarker(ureq, question);
		}
		
		private void doDeleteQuestion(VideoQuestion question) {
			cleanUp();
			
			questions.getQuestions().remove(question);
			videoManager.saveQuestions(questions, entry.getOlatResource());
			editorWrapper.setContent(null);
			
			loadModel(true, questions.getQuestions());
			reloadMarkers();
			selectTime(question.getBegin());
		}
	}
}
