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
package org.olat.ims.qti21.ui.editor;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.model.xml.interactions.DrawingAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.HotspotAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.KPrimAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MatchAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.UploadAssessmentItemBuilder;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.olat.ims.qti21.ui.editor.interactions.ChoiceScoreController;
import org.olat.ims.qti21.ui.editor.interactions.DrawingEditorController;
import org.olat.ims.qti21.ui.editor.interactions.EssayEditorController;
import org.olat.ims.qti21.ui.editor.interactions.FIBEditorController;
import org.olat.ims.qti21.ui.editor.interactions.FIBScoreController;
import org.olat.ims.qti21.ui.editor.interactions.HotspotChoiceScoreController;
import org.olat.ims.qti21.ui.editor.interactions.HotspotEditorController;
import org.olat.ims.qti21.ui.editor.interactions.KPrimEditorController;
import org.olat.ims.qti21.ui.editor.interactions.LobFeedbackEditorController;
import org.olat.ims.qti21.ui.editor.interactions.MatchEditorController;
import org.olat.ims.qti21.ui.editor.interactions.MatchScoreController;
import org.olat.ims.qti21.ui.editor.interactions.MultipleChoiceEditorController;
import org.olat.ims.qti21.ui.editor.interactions.SingleChoiceEditorController;
import org.olat.ims.qti21.ui.editor.interactions.UploadEditorController;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;

/**
 * 
 * Initial date: 22.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemEditorController extends BasicController {

	private final AssessmentItemRef itemRef;
	private final ResolvedAssessmentItem resolvedAssessmentItem;
	
	private final TabbedPane tabbedPane;
	private final VelocityContainer mainVC;
	
	private final int displayTabPosition;
	private MetadataEditorController metadataEditor;
	private AssessmentItemPreviewController displayCtrl;
	private Controller itemEditor, scoreEditor, feedbackEditor;
	
	private final File itemFile;
	private final File rootDirectory;
	private final VFSContainer rootContainer;
	
	private final boolean restrictedEdit;
	private RepositoryEntry testEntry;
	private AssessmentItemBuilder itemBuilder;
	private ManifestMetadataBuilder metadataBuilder;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private AssessmentService assessmentService;
	
	
	public AssessmentItemEditorController(UserRequest ureq, WindowControl wControl,
			ResolvedAssessmentItem resolvedAssessmentItem,
			File rootDirectory, VFSContainer rootContainer, File itemFile, boolean restrictedEdit) {
		super(ureq, wControl);
		this.itemRef = null;
		this.itemFile = itemFile;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.restrictedEdit = restrictedEdit;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		
		mainVC = createVelocityContainer("assessment_item_editor");
		mainVC.contextPut("restrictedEdit", restrictedEdit);
		tabbedPane = new TabbedPane("itemTabs", getLocale());
		tabbedPane.addListener(this);
		mainVC.put("tabbedpane", tabbedPane);

		initItemEditor(ureq);
		
		displayCtrl = new AssessmentItemPreviewController(ureq, getWindowControl(), resolvedAssessmentItem, rootDirectory, itemFile);
		listenTo(displayCtrl);
		displayTabPosition = tabbedPane.addTab(translate("preview"), displayCtrl);
		
		putInitialPanel(mainVC);
	}
	
	public AssessmentItemEditorController(UserRequest ureq, WindowControl wControl, RepositoryEntry testEntry,
			ResolvedAssessmentItem resolvedAssessmentItem, AssessmentItemRef itemRef, ManifestMetadataBuilder metadataBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile, boolean restrictedEdit) {
		super(ureq, wControl);
		this.itemRef = itemRef;
		this.metadataBuilder = metadataBuilder;
		this.itemFile = itemFile;
		this.testEntry = testEntry;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.restrictedEdit = restrictedEdit;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		
		mainVC = createVelocityContainer("assessment_item_editor");
		mainVC.contextPut("restrictedEdit", restrictedEdit);
		tabbedPane = new TabbedPane("itemTabs", getLocale());
		tabbedPane.addListener(this);
		mainVC.put("tabbedpane", tabbedPane);

		initItemEditor(ureq);
		
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(getIdentity(), null, testEntry, null, testEntry);
		displayCtrl = new AssessmentItemPreviewController(ureq, getWindowControl(),
				resolvedAssessmentItem, itemRef, testEntry, assessmentEntry, rootDirectory, itemFile);
		listenTo(displayCtrl);
		displayTabPosition = tabbedPane.addTab(translate("preview"), displayCtrl);
		
		putInitialPanel(mainVC);
	}
	
	public String getTitle() {
		return resolvedAssessmentItem.getRootNodeLookup().getRootNodeHolder().getRootNode().getTitle();
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private QTI21QuestionType initItemEditor(UserRequest ureq) {
		AssessmentItem item = resolvedAssessmentItem.getItemLookup().getRootNodeHolder().getRootNode();
		
		QTI21QuestionType type = QTI21QuestionType.getType(item);
		switch(type) {
			case sc: itemBuilder = initSingleChoiceEditors(ureq, item); break;
			case mc: itemBuilder = initMultipleChoiceEditors(ureq, item); break;
			case fib: itemBuilder = initFIBEditors(ureq, type, item); break;
			case numerical: itemBuilder = initFIBEditors(ureq, type, item); break;
			case kprim: itemBuilder = initKPrimChoiceEditors(ureq, item); break;
			case match: itemBuilder = initMatchChoiceEditors(ureq, item); break;
			case hotspot: itemBuilder = initHotspotEditors(ureq, item); break;
			case essay: itemBuilder = initEssayEditors(ureq, item); break;
			case upload: itemBuilder = initUploadEditors(ureq, item); break;
			case drawing: itemBuilder = initDrawingEditors(ureq, item); break;
			default: initItemCreatedByUnkownEditor(ureq, item); break;
		}
		
		if(metadataBuilder != null) {
			//metadataEditor = new MetadataEditorController(ureq, getWindowControl(), metadataBuilder);
			//listenTo(metadataEditor);
			//tabbedPane.addTab(translate("form.metadata"), metadataEditor);
		}
		return type;
	}
	
	private void initItemCreatedByUnkownEditor(UserRequest ureq, AssessmentItem item) {
		itemEditor = new UnkownItemEditorController(ureq, getWindowControl(), item);
		listenTo(itemEditor);
		tabbedPane.addTab(translate("form.unkown"), itemEditor);
	}

	private AssessmentItemBuilder initSingleChoiceEditors(UserRequest ureq, AssessmentItem item) {
		SingleChoiceAssessmentItemBuilder scItemBuilder = new SingleChoiceAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new SingleChoiceEditorController(ureq, getWindowControl(), scItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit);
		listenTo(itemEditor);
		scoreEditor = new ChoiceScoreController(ureq, getWindowControl(), scItemBuilder, itemRef, restrictedEdit,
				"Test editor QTI 2.1 in detail#details_testeditor_score");
		listenTo(scoreEditor);
		feedbackEditor = new FeedbackEditorController(ureq, getWindowControl(), scItemBuilder, restrictedEdit);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.choice"), itemEditor);
		tabbedPane.addTab(translate("form.score"), scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), feedbackEditor);
		return scItemBuilder;
	}
	
	private AssessmentItemBuilder initMultipleChoiceEditors(UserRequest ureq, AssessmentItem item) {
		MultipleChoiceAssessmentItemBuilder mcItemBuilder = new MultipleChoiceAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new MultipleChoiceEditorController(ureq, getWindowControl(), mcItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit);
		listenTo(itemEditor);
		scoreEditor = new ChoiceScoreController(ureq, getWindowControl(), mcItemBuilder, itemRef, restrictedEdit,
				"Test editor QTI 2.1 in detail#details_testeditor_score");
		listenTo(scoreEditor);
		feedbackEditor = new FeedbackEditorController(ureq, getWindowControl(), mcItemBuilder, restrictedEdit);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.choice"), itemEditor);
		tabbedPane.addTab(translate("form.score"), scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), feedbackEditor);
		return mcItemBuilder;
	}
	
	private AssessmentItemBuilder initKPrimChoiceEditors(UserRequest ureq, AssessmentItem item) {
		KPrimAssessmentItemBuilder kprimItemBuilder = new KPrimAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new KPrimEditorController(ureq, getWindowControl(), kprimItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit);
		listenTo(itemEditor);
		scoreEditor = new MinimalScoreController(ureq, getWindowControl(), kprimItemBuilder, itemRef, restrictedEdit,
				"Test editor QTI 2.1 in detail#details_testeditor_score");
		listenTo(scoreEditor);
		feedbackEditor = new FeedbackEditorController(ureq, getWindowControl(), kprimItemBuilder, restrictedEdit);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.kprim"), itemEditor);
		tabbedPane.addTab(translate("form.score"), scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), feedbackEditor);
		return kprimItemBuilder;
	}
	
	private AssessmentItemBuilder initMatchChoiceEditors(UserRequest ureq, AssessmentItem item) {
		MatchAssessmentItemBuilder matchItemBuilder = new MatchAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new MatchEditorController(ureq, getWindowControl(), matchItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit);
		listenTo(itemEditor);
		scoreEditor = new MatchScoreController(ureq, getWindowControl(), matchItemBuilder, itemRef, restrictedEdit);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbackEditorController(ureq, getWindowControl(), matchItemBuilder, restrictedEdit);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.match"), itemEditor);
		tabbedPane.addTab(translate("form.score"), scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), feedbackEditor);
		return matchItemBuilder;
	}
	
	private AssessmentItemBuilder initFIBEditors(UserRequest ureq, QTI21QuestionType preferedType, AssessmentItem item) {
		FIBAssessmentItemBuilder kprimItemBuilder = new FIBAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new FIBEditorController(ureq, getWindowControl(), preferedType, kprimItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit);
		listenTo(itemEditor);
		scoreEditor = new FIBScoreController(ureq, getWindowControl(), kprimItemBuilder, itemRef, restrictedEdit);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbackEditorController(ureq, getWindowControl(), kprimItemBuilder, restrictedEdit);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.fib"), itemEditor);
		tabbedPane.addTab(translate("form.score"), scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), feedbackEditor);
		return kprimItemBuilder;
	}
	
	private AssessmentItemBuilder initHotspotEditors(UserRequest ureq, AssessmentItem item) {
		HotspotAssessmentItemBuilder hotspotItemBuilder = new HotspotAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new HotspotEditorController(ureq, getWindowControl(), hotspotItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit);
		listenTo(itemEditor);
		scoreEditor = new HotspotChoiceScoreController(ureq, getWindowControl(), hotspotItemBuilder, itemRef, itemFile, restrictedEdit);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbackEditorController(ureq, getWindowControl(), hotspotItemBuilder, restrictedEdit);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.hotspot"), itemEditor);
		tabbedPane.addTab(translate("form.score"), scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), feedbackEditor);
		return hotspotItemBuilder;
	}
	
	private AssessmentItemBuilder initEssayEditors(UserRequest ureq, AssessmentItem item) {
		EssayAssessmentItemBuilder essayItemBuilder = new EssayAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new EssayEditorController(ureq, getWindowControl(), essayItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit);
		listenTo(itemEditor);
		scoreEditor = new MinimalScoreController(ureq, getWindowControl(), essayItemBuilder, itemRef, restrictedEdit,
				"Test editor QTI 2.1 in detail#details_testeditor_score");
		listenTo(scoreEditor);
		feedbackEditor = new LobFeedbackEditorController(ureq, getWindowControl(), essayItemBuilder, restrictedEdit);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.essay"), itemEditor);
		tabbedPane.addTab(translate("form.score"), scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), feedbackEditor);
		return essayItemBuilder;
	}
	
	private AssessmentItemBuilder initUploadEditors(UserRequest ureq, AssessmentItem item) {
		UploadAssessmentItemBuilder uploadItemBuilder = new UploadAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new UploadEditorController(ureq, getWindowControl(), uploadItemBuilder,
				rootDirectory, rootContainer, itemFile);
		listenTo(itemEditor);
		scoreEditor = new MinimalScoreController(ureq, getWindowControl(), uploadItemBuilder, itemRef, restrictedEdit,
				"Test editor QTI 2.1 in detail#details_testeditor_score");
		listenTo(scoreEditor);
		feedbackEditor = new LobFeedbackEditorController(ureq, getWindowControl(), uploadItemBuilder, restrictedEdit);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.upload"), itemEditor);
		tabbedPane.addTab(translate("form.score"), scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), feedbackEditor);
		return uploadItemBuilder;
	}
	
	private AssessmentItemBuilder initDrawingEditors(UserRequest ureq, AssessmentItem item) {
		DrawingAssessmentItemBuilder uploadItemBuilder = new DrawingAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new DrawingEditorController(ureq, getWindowControl(), uploadItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit);
		listenTo(itemEditor);
		scoreEditor = new MinimalScoreController(ureq, getWindowControl(), uploadItemBuilder, itemRef, restrictedEdit,
				"Test and Questionnaire Editor in Detail#details_testeditor_fragetypen_ft");
		listenTo(scoreEditor);
		feedbackEditor = new LobFeedbackEditorController(ureq, getWindowControl(), uploadItemBuilder, restrictedEdit);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.drawing"), itemEditor);
		tabbedPane.addTab(translate("form.score"), scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), feedbackEditor);
		return uploadItemBuilder;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(tabbedPane == source) {
			Controller selectedCtrl = tabbedPane.getSelectedController();
			if(selectedCtrl instanceof SyncAssessmentItem) {
				((SyncAssessmentItem)selectedCtrl).sync(ureq, itemBuilder);
			} else if(selectedCtrl == displayCtrl) {
				if(testEntry != null) {
					AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(getIdentity(), null, testEntry, null, testEntry);
					displayCtrl = new AssessmentItemPreviewController(ureq, getWindowControl(),
						resolvedAssessmentItem, itemRef, testEntry, assessmentEntry, rootDirectory, itemFile);
				} else {
					displayCtrl = new AssessmentItemPreviewController(ureq, getWindowControl(), resolvedAssessmentItem, rootDirectory, itemFile);
				}
				
				listenTo(displayCtrl);
				tabbedPane.replaceTab(displayTabPosition, displayCtrl);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof AssessmentItemEvent) {
			if(event instanceof AssessmentItemEvent) {
				AssessmentItemEvent aie = (AssessmentItemEvent)event;
				if(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED.equals(aie.getCommand())) {
					doBuildAndSaveAssessmentItem();
					doBuildAndCommitMetadata();
					fireEvent(ureq, new AssessmentItemEvent(aie.getCommand(), aie.getAssessmentItem(), itemRef, aie.getQuestionType()));
				}
			}
		} else if(metadataEditor == source) {
			if(event == Event.CHANGED_EVENT) {
				doBuildAndCommitMetadata();
				AssessmentItem item = resolvedAssessmentItem.getItemLookup().getRootNodeHolder().getRootNode();
				fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_METADATA_CHANGED, item, itemRef, null));
			}
		}
		super.event(ureq, source, event);
	}

	private void doBuildAndSaveAssessmentItem() {
		//update assessment item file
		if(itemBuilder != null) {
			itemBuilder.build();
		}
		qtiService.updateAssesmentObject(itemFile, resolvedAssessmentItem);
	}

	private void doBuildAndCommitMetadata() {
		if(metadataBuilder == null) return;
		
		//update manifest
		metadataBuilder.setTechnicalFormat(ManifestBuilder.ASSESSMENTITEM_MIMETYPE);
		if(itemBuilder != null) {
			metadataBuilder.setQtiMetadata(itemBuilder.getInteractionNames());
			metadataBuilder.setOpenOLATMetadataQuestionType(itemBuilder.getQuestionType().getPrefix());
		} else {
			metadataBuilder.setOpenOLATMetadataQuestionType(QTI21QuestionType.unkown.getPrefix());
		}
	}
}