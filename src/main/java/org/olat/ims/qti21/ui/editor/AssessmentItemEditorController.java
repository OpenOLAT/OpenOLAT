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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
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
import org.olat.ims.qti21.model.xml.interactions.HottextAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.InlineChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.KPrimAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MatchAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.OrderAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.UploadAssessmentItemBuilder;
import org.olat.ims.qti21.ui.AssessmentItemDisplayController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.olat.ims.qti21.ui.editor.events.DetachFromPoolEvent;
import org.olat.ims.qti21.ui.editor.events.SelectEvent.SelectionTarget;
import org.olat.ims.qti21.ui.editor.interactions.ChoiceScoreController;
import org.olat.ims.qti21.ui.editor.interactions.DrawingEditorController;
import org.olat.ims.qti21.ui.editor.interactions.EssayEditorController;
import org.olat.ims.qti21.ui.editor.interactions.FIBEditorController;
import org.olat.ims.qti21.ui.editor.interactions.FIBScoreController;
import org.olat.ims.qti21.ui.editor.interactions.HotspotChoiceScoreController;
import org.olat.ims.qti21.ui.editor.interactions.HotspotEditorController;
import org.olat.ims.qti21.ui.editor.interactions.HottextEditorController;
import org.olat.ims.qti21.ui.editor.interactions.InlineChoiceEditorController;
import org.olat.ims.qti21.ui.editor.interactions.InlineChoiceScoreController;
import org.olat.ims.qti21.ui.editor.interactions.KPrimEditorController;
import org.olat.ims.qti21.ui.editor.interactions.MatchEditorController;
import org.olat.ims.qti21.ui.editor.interactions.MatchScoreController;
import org.olat.ims.qti21.ui.editor.interactions.MultipleChoiceEditorController;
import org.olat.ims.qti21.ui.editor.interactions.OrderEditorController;
import org.olat.ims.qti21.ui.editor.interactions.OrderScoreController;
import org.olat.ims.qti21.ui.editor.interactions.SingleChoiceEditorController;
import org.olat.ims.qti21.ui.editor.interactions.TrueFalseEditorController;
import org.olat.ims.qti21.ui.editor.interactions.UploadEditorController;
import org.olat.ims.qti21.ui.editor.metadata.MetadataChangedEvent;
import org.olat.ims.qti21.ui.editor.metadata.MetadataController;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
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
public class AssessmentItemEditorController extends BasicController implements Activateable2 {

	private final AssessmentItemRef itemRef;
	private final ResolvedAssessmentItem resolvedAssessmentItem;
	
	private TabbedPane tabbedPane;
	private final VelocityContainer mainVC;
	
	private int displayTabPosition;
	private int solutionTabPosition;
	
	private Controller itemEditor;
	private Controller scoreEditor;
	private Controller feedbackEditor;
	private PoolEditorController poolEditor;
	private MetadataController metadataCtrl;
	private AssessmentItemPreviewController displayCtrl;
	private AssessmentItemPreviewSolutionController solutionCtrl;
	
	private final File itemFile;
	private final File rootDirectory;
	private final VFSContainer rootContainer;
	
	private final boolean readOnly;
	private final boolean restrictedEdit;
	private RepositoryEntry testEntry;
	private AssessmentItemBuilder itemBuilder;
	private ManifestMetadataBuilder metadataBuilder;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private AssessmentService assessmentService;
	
	public AssessmentItemEditorController(UserRequest ureq, WindowControl wControl,
			ResolvedAssessmentItem resolvedAssessmentItem,
			File rootDirectory, VFSContainer rootContainer, File itemFile, boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentItemDisplayController.class, ureq.getLocale()));
		this.itemRef = null;
		this.itemFile = itemFile;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.readOnly = readOnly;
		this.restrictedEdit = restrictedEdit;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		
		if(resolvedAssessmentItem == null || resolvedAssessmentItem.getItemLookup() == null
				|| resolvedAssessmentItem.getItemLookup().getRootNodeHolder() == null) {
			mainVC = createVelocityContainer("missing_resource");
			mainVC.contextPut("uri", itemFile == null ? "" : itemFile);
		} else {
			mainVC = createVelocityContainer("assessment_item_editor");
			mainVC.contextPut("restrictedEdit", restrictedEdit);
			tabbedPane = new TabbedPane("itemTabs", getLocale());
			tabbedPane.setElementCssClass("o_sel_assessment_item_config");
			tabbedPane.addListener(this);
			mainVC.put("tabbedpane", tabbedPane);
	
			initItemEditor(ureq);
			
			displayCtrl = new AssessmentItemPreviewController(ureq, getWindowControl(), resolvedAssessmentItem, rootDirectory, itemFile);
			listenTo(displayCtrl);
			displayTabPosition = tabbedPane.addTab(translate("preview"), "o_sel_assessment_item_preview", displayCtrl);
		
			solutionCtrl = new AssessmentItemPreviewSolutionController(ureq, getWindowControl(), resolvedAssessmentItem, rootDirectory, itemFile);
			listenTo(solutionCtrl);
			solutionTabPosition = tabbedPane.addTab(translate("preview.solution"), "o_sel_assessment_item_solution", solutionCtrl);
		}
		
		putInitialPanel(mainVC);
	}
	
	public AssessmentItemEditorController(UserRequest ureq, WindowControl wControl, RepositoryEntry testEntry,
			ResolvedAssessmentItem resolvedAssessmentItem, AssessmentItemRef itemRef, ManifestMetadataBuilder metadataBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile, boolean restrictedEdit) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentItemDisplayController.class, ureq.getLocale()));
		this.itemRef = itemRef;
		this.metadataBuilder = metadataBuilder;
		this.itemFile = itemFile;
		this.testEntry = testEntry;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.restrictedEdit = restrictedEdit;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		
		if(resolvedAssessmentItem == null || resolvedAssessmentItem.getItemLookup() == null
				|| resolvedAssessmentItem.getItemLookup().getRootNodeHolder() == null) {
			mainVC = createVelocityContainer("missing_resource");
			mainVC.contextPut("uri", itemFile == null ? "" : itemFile);
			readOnly = true;
		} else {
			mainVC = createVelocityContainer("assessment_item_editor");
			mainVC.contextPut("restrictedEdit", restrictedEdit);
			tabbedPane = new TabbedPane("itemTabs", getLocale());
			tabbedPane.setElementCssClass("o_sel_assessment_item_config");
			tabbedPane.addListener(this);
			mainVC.put("tabbedpane", tabbedPane);
			
			//check the status in the pool
			QPoolInformations qStatus = getPoolStatus();
			readOnly = qStatus.isReadOnly();
			if(qStatus.isPooled()) {
				poolEditor = new PoolEditorController(ureq, getWindowControl(), itemRef, metadataBuilder, qStatus);
				listenTo(poolEditor);
			}

			initItemEditor(ureq);
			
			AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(getIdentity(), null, testEntry, null, Boolean.TRUE, testEntry);
			displayCtrl = new AssessmentItemPreviewController(ureq, getWindowControl(),
					resolvedAssessmentItem, itemRef, testEntry, assessmentEntry, rootDirectory, itemFile);
			listenTo(displayCtrl);
			displayTabPosition = tabbedPane.addTab(translate("preview"), "o_sel_assessment_item_preview", displayCtrl);

			solutionCtrl = new AssessmentItemPreviewSolutionController(ureq, getWindowControl(), resolvedAssessmentItem, rootDirectory, itemFile);
			listenTo(solutionCtrl);
			solutionTabPosition = tabbedPane.addTab(translate("preview.solution"), "o_sel_assessment_item_solution", solutionCtrl);
			
			if(poolEditor != null ) {
				tabbedPane.addTab(translate("form.pool"), poolEditor);
			}
			
			if(metadataBuilder != null) {
				metadataCtrl = new MetadataController(ureq, getWindowControl(), metadataBuilder, readOnly);
				listenTo(metadataCtrl);
				tabbedPane.addTab(translate("form.metadata"), metadataCtrl);
			}
		}

		putInitialPanel(mainVC);
	}
	
	public TabbedPane getTabbedPane() {
		return tabbedPane;
	}
	
	public void addTab(int pos, String displayName, Controller controller) {
		tabbedPane.addTab(pos, displayName, controller);
	}
	
	public QPoolInformations getPoolStatus() {
		boolean isReadOnly = false;
		boolean pooled = false;
		QuestionItem originalItem = null;
		QuestionItem masterItem = null;
		if(metadataBuilder != null) {
			if(StringHelper.containsNonWhitespace(metadataBuilder.getOpenOLATMetadataIdentifier())) {
				List<QuestionItem> items = qpoolService.loadItemByIdentifier(metadataBuilder.getOpenOLATMetadataIdentifier());
				if(!items.isEmpty()) {
					pooled = true;
					isReadOnly = true;
					if(items.size() == 1) {
						originalItem = items.get(0);
					}
				}
			}

			if(StringHelper.containsNonWhitespace(metadataBuilder.getOpenOLATMetadataIdentifier())) {
				List<QuestionItem> items = qpoolService.loadItemByIdentifier(metadataBuilder.getOpenOLATMetadataMasterIdentifier());
				if(!items.isEmpty()) {
					pooled = true;
					if(items.size() == 1) {
						masterItem = items.get(0);
					}
				}
			}
			
		}
		return new QPoolInformations(isReadOnly, pooled, originalItem, masterItem);
	}
	
	public String getTitle() {
		return resolvedAssessmentItem.getRootNodeLookup().getRootNodeHolder().getRootNode().getTitle();
	}
	
	public AssessmentItem getAssessmentItem() {
		return resolvedAssessmentItem.getRootNodeLookup().getRootNodeHolder().getRootNode();
	}
	
	public QTI21QuestionType getType() {
		return itemBuilder.getQuestionType();
	}
	
	private QTI21QuestionType initItemEditor(UserRequest ureq) {
		AssessmentItem item = resolvedAssessmentItem.getItemLookup().getRootNodeHolder().getRootNode();
		
		QTI21QuestionType type = QTI21QuestionType.getType(item);
		switch(type) {
			case sc: itemBuilder = initSingleChoiceEditors(ureq, item); break;
			case mc: itemBuilder = initMultipleChoiceEditors(ureq, item); break;
			case fib: itemBuilder = initFIBEditors(ureq, item); break;
			case numerical: itemBuilder = initFIBEditors(ureq, item); break;
			case kprim: itemBuilder = initKPrimChoiceEditors(ureq, item); break;
			case match: itemBuilder = initMatchChoiceEditors(ureq, item); break;
			case matchdraganddrop: itemBuilder = initMatchDragAndDropEditors(ureq, item); break;
			case matchtruefalse: itemBuilder = initMatchTrueFalseEditors(ureq, item); break;
			case hotspot: itemBuilder = initHotspotEditors(ureq, item); break;
			case essay: itemBuilder = initEssayEditors(ureq, item); break;
			case upload: itemBuilder = initUploadEditors(ureq, item); break;
			case drawing: itemBuilder = initDrawingEditors(ureq, item); break;
			case hottext: itemBuilder = initHottextEditors(ureq, item); break;
			case order: itemBuilder = initOrderEditors(ureq, item); break;
			case inlinechoice: itemBuilder = initInlineChoiceEditors(ureq, item); break;
			default: initItemCreatedByUnkownEditor(ureq, item); break;
		}
		return type;
	}
	
	private void initItemCreatedByUnkownEditor(UserRequest ureq, AssessmentItem item) {
		itemEditor = new UnkownItemEditorController(ureq, getWindowControl(), resolvedAssessmentItem, item, itemFile, rootDirectory);
		listenTo(itemEditor);
		tabbedPane.addTab(translate("form.unkown"), itemEditor);
	}

	private AssessmentItemBuilder initSingleChoiceEditors(UserRequest ureq, AssessmentItem item) {
		SingleChoiceAssessmentItemBuilder scItemBuilder = new SingleChoiceAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new SingleChoiceEditorController(ureq, getWindowControl(), scItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit, readOnly);
		listenTo(itemEditor);
		scoreEditor = new ChoiceScoreController(ureq, getWindowControl(), scItemBuilder, itemRef, itemFile, rootDirectory, restrictedEdit, readOnly);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbacksEditorController(ureq, getWindowControl(), scItemBuilder,
				rootDirectory, rootContainer, itemFile, FeedbacksEnabler.standardFeedbacks(),
				restrictedEdit, readOnly);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.choice"), "o_sel_assessment_item_choice", itemEditor);
		tabbedPane.addTab(translate("form.score"), "o_sel_assessment_item_score", scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), "o_sel_assessment_item_feedback", feedbackEditor);
		return scItemBuilder;
	}
	
	private AssessmentItemBuilder initMultipleChoiceEditors(UserRequest ureq, AssessmentItem item) {
		MultipleChoiceAssessmentItemBuilder mcItemBuilder = new MultipleChoiceAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new MultipleChoiceEditorController(ureq, getWindowControl(), mcItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit, readOnly);
		listenTo(itemEditor);
		scoreEditor = new ChoiceScoreController(ureq, getWindowControl(), mcItemBuilder, itemRef, itemFile, rootDirectory, restrictedEdit, readOnly);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbacksEditorController(ureq, getWindowControl(), mcItemBuilder,
				rootDirectory, rootContainer, itemFile, FeedbacksEnabler.standardFeedbacks(),
				restrictedEdit, readOnly);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.choice"), "o_sel_assessment_item_choice", itemEditor);
		tabbedPane.addTab(translate("form.score"), "o_sel_assessment_item_score", scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), "o_sel_assessment_item_feedback", feedbackEditor);
		return mcItemBuilder;
	}
	
	private AssessmentItemBuilder initKPrimChoiceEditors(UserRequest ureq, AssessmentItem item) {
		KPrimAssessmentItemBuilder kprimItemBuilder = new KPrimAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new KPrimEditorController(ureq, getWindowControl(), kprimItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit, readOnly);
		listenTo(itemEditor);
		scoreEditor = new MinimalScoreController(ureq, getWindowControl(), kprimItemBuilder, itemRef, restrictedEdit, readOnly);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbacksEditorController(ureq, getWindowControl(), kprimItemBuilder,
				rootDirectory, rootContainer, itemFile, FeedbacksEnabler.standardFeedbacks(),
				restrictedEdit, readOnly);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.kprim"), "o_sel_assessment_item_kprim", itemEditor);
		tabbedPane.addTab(translate("form.score"), "o_sel_assessment_item_score", scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), "o_sel_assessment_item_feedback", feedbackEditor);
		return kprimItemBuilder;
	}
	
	private AssessmentItemBuilder initMatchChoiceEditors(UserRequest ureq, AssessmentItem item) {
		MatchAssessmentItemBuilder matchItemBuilder = new MatchAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new MatchEditorController(ureq, getWindowControl(), matchItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit, readOnly);
		listenTo(itemEditor);
		scoreEditor = new MatchScoreController(ureq, getWindowControl(), matchItemBuilder, itemRef, itemFile, rootDirectory, true,
				restrictedEdit, readOnly);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbacksEditorController(ureq, getWindowControl(), matchItemBuilder,
				rootDirectory, rootContainer, itemFile, FeedbacksEnabler.standardFeedbacks(),
				restrictedEdit, readOnly);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.match"), "o_sel_assessment_item_match", itemEditor);
		tabbedPane.addTab(translate("form.score"), "o_sel_assessment_item_score", scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), "o_sel_assessment_item_feedback", feedbackEditor);
		return matchItemBuilder;
	}
	
	private AssessmentItemBuilder initMatchDragAndDropEditors(UserRequest ureq, AssessmentItem item) {
		MatchAssessmentItemBuilder matchItemBuilder = new MatchAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new MatchEditorController(ureq, getWindowControl(), matchItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit, readOnly);
		listenTo(itemEditor);
		scoreEditor = new MatchScoreController(ureq, getWindowControl(), matchItemBuilder, itemRef, itemFile, rootDirectory, true,
				restrictedEdit, readOnly);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbacksEditorController(ureq, getWindowControl(), matchItemBuilder,
				rootDirectory, rootContainer, itemFile, FeedbacksEnabler.standardFeedbacks(),
				restrictedEdit, readOnly);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.matchdraganddrop"), "o_sel_assessment_item_dnd", itemEditor);
		tabbedPane.addTab(translate("form.score"), "o_sel_assessment_item_score", scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), "o_sel_assessment_item_feedback", feedbackEditor);
		return matchItemBuilder;
	}
	
	private AssessmentItemBuilder initMatchTrueFalseEditors(UserRequest ureq, AssessmentItem item) {
		MatchAssessmentItemBuilder matchItemBuilder = new MatchAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new TrueFalseEditorController(ureq, getWindowControl(), matchItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit, readOnly);
		listenTo(itemEditor);
		scoreEditor = new MatchScoreController(ureq, getWindowControl(), matchItemBuilder, itemRef, itemFile, rootDirectory,false,
				restrictedEdit, readOnly);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbacksEditorController(ureq, getWindowControl(), matchItemBuilder,
				rootDirectory, rootContainer, itemFile, FeedbacksEnabler.standardFeedbacks(),
				restrictedEdit, readOnly);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.matchtruefalse"), "o_sel_assessment_item_truefalse", itemEditor);
		tabbedPane.addTab(translate("form.score"), "o_sel_assessment_item_score", scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), "o_sel_assessment_item_feedback", feedbackEditor);
		return matchItemBuilder;
	}
	
	private AssessmentItemBuilder initFIBEditors(UserRequest ureq, AssessmentItem item) {
		FIBAssessmentItemBuilder fibItemBuilder = new FIBAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new FIBEditorController(ureq, getWindowControl(), fibItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit, readOnly);
		listenTo(itemEditor);
		scoreEditor = new FIBScoreController(ureq, getWindowControl(), fibItemBuilder, itemRef,
				restrictedEdit, readOnly);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbacksEditorController(ureq, getWindowControl(), fibItemBuilder,
				rootDirectory, rootContainer, itemFile, FeedbacksEnabler.standardFeedbacks(),
				restrictedEdit, readOnly);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.fib"), "o_sel_assessment_item_fib", itemEditor);
		tabbedPane.addTab(translate("form.score"), "o_sel_assessment_item_score", scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), "o_sel_assessment_item_feedback", feedbackEditor);
		return fibItemBuilder;
	}
	
	private AssessmentItemBuilder initHotspotEditors(UserRequest ureq, AssessmentItem item) {
		HotspotAssessmentItemBuilder hotspotItemBuilder = new HotspotAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new HotspotEditorController(ureq, getWindowControl(), hotspotItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit, readOnly);
		listenTo(itemEditor);
		scoreEditor = new HotspotChoiceScoreController(ureq, getWindowControl(), hotspotItemBuilder, itemRef, itemFile,
				restrictedEdit, readOnly);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbacksEditorController(ureq, getWindowControl(), hotspotItemBuilder,
				rootDirectory, rootContainer, itemFile, FeedbacksEnabler.standardFeedbacks(),
				restrictedEdit, readOnly);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.hotspot"), "o_sel_assessment_item_hotspot", itemEditor);
		tabbedPane.addTab(translate("form.score"), "o_sel_assessment_item_score", scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), "o_sel_assessment_item_feedback", feedbackEditor);
		return hotspotItemBuilder;
	}
	
	private AssessmentItemBuilder initEssayEditors(UserRequest ureq, AssessmentItem item) {
		EssayAssessmentItemBuilder essayItemBuilder = new EssayAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new EssayEditorController(ureq, getWindowControl(), essayItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit, readOnly);
		listenTo(itemEditor);
		scoreEditor = new MinimalScoreController(ureq, getWindowControl(), essayItemBuilder, itemRef, restrictedEdit, readOnly);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbacksEditorController(ureq, getWindowControl(), essayItemBuilder,
				rootDirectory, rootContainer, itemFile, FeedbacksEnabler.lobFeedbacks(),
				restrictedEdit, readOnly);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.essay"), "o_sel_assessment_item_essay", itemEditor);
		tabbedPane.addTab(translate("form.score"), "o_sel_assessment_item_score", scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), "o_sel_assessment_item_feedback", feedbackEditor);
		return essayItemBuilder;
	}
	
	private AssessmentItemBuilder initUploadEditors(UserRequest ureq, AssessmentItem item) {
		UploadAssessmentItemBuilder uploadItemBuilder = new UploadAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new UploadEditorController(ureq, getWindowControl(), uploadItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit, readOnly);
		listenTo(itemEditor);
		scoreEditor = new MinimalScoreController(ureq, getWindowControl(), uploadItemBuilder, itemRef, restrictedEdit, readOnly);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbacksEditorController(ureq, getWindowControl(), uploadItemBuilder,
				rootDirectory, rootContainer, itemFile, FeedbacksEnabler.lobFeedbacks(),
				restrictedEdit, readOnly);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.upload"), "o_sel_assessment_item_upload", itemEditor);
		tabbedPane.addTab(translate("form.score"), "o_sel_assessment_item_score", scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), "o_sel_assessment_item_feedback", feedbackEditor);
		return uploadItemBuilder;
	}
	
	private AssessmentItemBuilder initDrawingEditors(UserRequest ureq, AssessmentItem item) {
		DrawingAssessmentItemBuilder uploadItemBuilder = new DrawingAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new DrawingEditorController(ureq, getWindowControl(), uploadItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit, readOnly);
		listenTo(itemEditor);
		scoreEditor = new MinimalScoreController(ureq, getWindowControl(), uploadItemBuilder, itemRef, restrictedEdit, readOnly);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbacksEditorController(ureq, getWindowControl(), uploadItemBuilder,
				rootDirectory, rootContainer, itemFile, FeedbacksEnabler.lobFeedbacks(),
				restrictedEdit, readOnly);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.drawing"), "o_sel_assessment_item_drawing", itemEditor);
		tabbedPane.addTab(translate("form.score"), "o_sel_assessment_item_score", scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), "o_sel_assessment_item_feedback", feedbackEditor);
		return uploadItemBuilder;
	}
	
	private AssessmentItemBuilder initHottextEditors(UserRequest ureq, AssessmentItem item) {
		HottextAssessmentItemBuilder hottextItemBuilder = new HottextAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new HottextEditorController(ureq, getWindowControl(), hottextItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit, readOnly);
		listenTo(itemEditor);
		scoreEditor = new ChoiceScoreController(ureq, getWindowControl(), hottextItemBuilder, itemRef, itemFile, rootDirectory, restrictedEdit, readOnly);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbacksEditorController(ureq, getWindowControl(), hottextItemBuilder,
				rootDirectory, rootContainer, itemFile, FeedbacksEnabler.standardFeedbacks(),
				restrictedEdit, readOnly);
		listenTo(feedbackEditor);

		tabbedPane.addTab(translate("form.hottext"), "o_sel_assessment_item_hottext", itemEditor);
		tabbedPane.addTab(translate("form.score"), "o_sel_assessment_item_score", scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), "o_sel_assessment_item_feedback", feedbackEditor);
		return hottextItemBuilder;
	}
	
	private AssessmentItemBuilder initOrderEditors(UserRequest ureq, AssessmentItem item) {
		OrderAssessmentItemBuilder orderItemBuilder = new OrderAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new OrderEditorController(ureq, getWindowControl(), orderItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit, readOnly);
		listenTo(itemEditor);
		scoreEditor = new OrderScoreController(ureq, getWindowControl(), orderItemBuilder, itemRef, restrictedEdit, readOnly);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbacksEditorController(ureq, getWindowControl(), orderItemBuilder,
				rootDirectory, rootContainer, itemFile, FeedbacksEnabler.standardFeedbacks(),
				restrictedEdit, readOnly);
		listenTo(feedbackEditor);

		tabbedPane.addTab(translate("form.order"), "o_sel_assessment_item_order", itemEditor);
		tabbedPane.addTab(translate("form.score"), "o_sel_assessment_item_score", scoreEditor);
		tabbedPane.addTab(translate("form.feedback"), "o_sel_assessment_item_feedback", feedbackEditor);
		return orderItemBuilder;
	}
	
	private AssessmentItemBuilder initInlineChoiceEditors(UserRequest ureq, AssessmentItem item) {
		InlineChoiceAssessmentItemBuilder inlineChoiceItemBuilder = new InlineChoiceAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new InlineChoiceEditorController(ureq, getWindowControl(), inlineChoiceItemBuilder,
				rootDirectory, rootContainer, itemFile, restrictedEdit, readOnly);
		listenTo(itemEditor);
		
		feedbackEditor = new FeedbacksEditorController(ureq, getWindowControl(), inlineChoiceItemBuilder,
				rootDirectory, rootContainer, itemFile, FeedbacksEnabler.standardFeedbacks(),
				restrictedEdit, readOnly);
		listenTo(feedbackEditor);

		tabbedPane.addTab(translate("form.inlinechoice"), "o_sel_assessment_item_inlinechoice", itemEditor);
		tabbedPane.addTabControllerCreator(ureq, translate("form.score"), "o_sel_assessment_item_score", uureq -> {
			removeAsListenerAndDispose(scoreEditor);
			scoreEditor = new InlineChoiceScoreController(uureq, getWindowControl(), inlineChoiceItemBuilder, itemRef, restrictedEdit, readOnly);
			listenTo(scoreEditor);
			return scoreEditor;
		}, true);
		tabbedPane.addTab(translate("form.feedback"), "o_sel_assessment_item_feedback", feedbackEditor);
		return inlineChoiceItemBuilder;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(SelectionTarget.description.name().equalsIgnoreCase(type)
				|| SelectionTarget.expert.name().equalsIgnoreCase(type)) {
			activate(ureq, itemEditor);
		} else if(SelectionTarget.attempts.name().equalsIgnoreCase(type)
				|| SelectionTarget.maxpoints.name().equalsIgnoreCase(type)) {
			activate(ureq, scoreEditor);
		} else if(SelectionTarget.feedback.name().equalsIgnoreCase(type)) {
			activate(ureq, feedbackEditor);
		}
	}
	
	private void activate(UserRequest ureq, Controller ctrl) {
		if(ctrl == null) return;
		
		int index = tabbedPane.indexOfTab(ctrl.getInitialComponent());
		if(index >= 0) {
			tabbedPane.setSelectedPane(ureq, index);
		}	
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(tabbedPane == source) {
			Controller selectedCtrl = tabbedPane.getSelectedController();
			if(selectedCtrl instanceof SyncAssessmentItem) {
				((SyncAssessmentItem)selectedCtrl).sync(ureq, itemBuilder);
			} else if(selectedCtrl == displayCtrl) {
				removeAsListenerAndDispose(displayCtrl);

				if(testEntry != null) {
					AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(getIdentity(), null, testEntry, null, Boolean.TRUE, testEntry);
					displayCtrl = new AssessmentItemPreviewController(ureq, getWindowControl(),
						resolvedAssessmentItem, itemRef, testEntry, assessmentEntry, rootDirectory, itemFile);
				} else {
					displayCtrl = new AssessmentItemPreviewController(ureq, getWindowControl(), resolvedAssessmentItem, rootDirectory, itemFile);
				}
				
				listenTo(displayCtrl);
				tabbedPane.replaceTab(displayTabPosition, displayCtrl);
			} else if(selectedCtrl == solutionCtrl) {
				removeAsListenerAndDispose(solutionCtrl);
				
				solutionCtrl = new AssessmentItemPreviewSolutionController(ureq, getWindowControl(), resolvedAssessmentItem, rootDirectory, itemFile);
				listenTo(displayCtrl);
				tabbedPane.replaceTab(solutionTabPosition, solutionCtrl);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof AssessmentItemEvent) {
			AssessmentItemEvent aie = (AssessmentItemEvent)event;
			if(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED.equals(aie.getCommand())) {
				doBuildAndSaveAssessmentItem();
				doBuildAndCommitMetadata();
				fireEvent(ureq, new AssessmentItemEvent(aie.getCommand(), aie.getAssessmentItem(), itemRef, aie.getQuestionType()));
			} else if(AssessmentItemEvent.ASSESSMENT_ITEM_NEED_RELOAD.equals(aie.getCommand())) {
				fireEvent(ureq, event);
			}
		} else if(poolEditor == source) {
			if(event instanceof DetachFromPoolEvent) {
				fireEvent(ureq, event);
			}
		} else if(metadataCtrl == source) {
			if(event instanceof MetadataChangedEvent) {
				doBuildAndCommitMetadata();
				fireEvent(ureq, event);
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
			metadataBuilder.setQtiMetadataInteractionTypes(itemBuilder.getInteractionNames());
			metadataBuilder.setOpenOLATMetadataQuestionType(itemBuilder.getQuestionType().getPrefix());
		} else {
			metadataBuilder.setOpenOLATMetadataQuestionType(QTI21QuestionType.unkown.getPrefix());
		}
	}
}