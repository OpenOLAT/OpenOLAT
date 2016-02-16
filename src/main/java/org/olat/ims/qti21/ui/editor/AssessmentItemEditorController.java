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
import java.net.URI;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.QTI21QuestionTypeDetector;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.KPrimAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.ui.AssessmentItemDisplayController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.olat.ims.qti21.ui.editor.interactions.EssayEditorController;
import org.olat.ims.qti21.ui.editor.interactions.KPrimEditorController;
import org.olat.ims.qti21.ui.editor.interactions.MultipleChoiceEditorController;
import org.olat.ims.qti21.ui.editor.interactions.SingleChoiceEditorController;
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
	
	private Controller itemEditor, scoreEditor, feedbackEditor;
	private AssessmentItemDisplayController displayCtrl;
	
	private AssessmentItemBuilder itemBuilder;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private AssessmentService assessmentService;
	
	
	public AssessmentItemEditorController(UserRequest ureq, WindowControl wControl,
			ResolvedAssessmentItem resolvedAssessmentItem, File unzippedDirectory, File itemFile) {
		super(ureq, wControl);
		this.itemRef = null;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		
		mainVC = createVelocityContainer("assessment_item_editor");
		tabbedPane = new TabbedPane("itemTabs", getLocale());
		tabbedPane.addListener(this);
		mainVC.put("tabbedpane", tabbedPane);

		initItemEditor(ureq);
		
		displayCtrl = new AssessmentItemDisplayController(ureq, getWindowControl(), resolvedAssessmentItem, unzippedDirectory, itemFile);
		listenTo(displayCtrl);
		tabbedPane.addTab("Preview", displayCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	public AssessmentItemEditorController(UserRequest ureq, WindowControl wControl, RepositoryEntry testEntry,
			ResolvedAssessmentItem resolvedAssessmentItem, AssessmentItemRef itemRef, File unzippedDirectory) {
		super(ureq, wControl);
		this.itemRef = itemRef;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		
		mainVC = createVelocityContainer("assessment_item_editor");
		tabbedPane = new TabbedPane("itemTabs", getLocale());
		tabbedPane.addListener(this);
		mainVC.put("tabbedpane", tabbedPane);

		initItemEditor(ureq);
		
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(getIdentity(), testEntry, null, testEntry);
		displayCtrl = new AssessmentItemDisplayController(ureq, getWindowControl(),
				testEntry, assessmentEntry, true, resolvedAssessmentItem, itemRef, unzippedDirectory);
		listenTo(displayCtrl);
		tabbedPane.addTab("Preview", displayCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	public String getTitle() {
		return resolvedAssessmentItem.getRootNodeLookup().getRootNodeHolder().getRootNode().getTitle();
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void initItemEditor(UserRequest ureq) {
		
		
		
		AssessmentItem item = resolvedAssessmentItem.getItemLookup().getRootNodeHolder().getRootNode();
		QTI21QuestionType type = QTI21QuestionTypeDetector.getType(item);
		switch(type) {
			case sc: itemBuilder = initSingleChoiceEditors(ureq, item); break;
			case mc: itemBuilder = initMultipleChoiceEditors(ureq, item); break;
			case kprim: itemBuilder = initKPrimChoiceEditors(ureq, item); break;
			case essay: itemBuilder = initEssayEditors(ureq, item); break;
			default: initItemCreatedByUnkownEditor(ureq); break;
		}

	}
	
	private void initItemCreatedByUnkownEditor(UserRequest ureq) {
		itemEditor = new UnkownItemEditorController(ureq, getWindowControl());
		listenTo(itemEditor);
		tabbedPane.addTab("Unkown", itemEditor.getInitialComponent());
	}

	private AssessmentItemBuilder initSingleChoiceEditors(UserRequest ureq, AssessmentItem item) {
		SingleChoiceAssessmentItemBuilder scItemBuilder = new SingleChoiceAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new SingleChoiceEditorController(ureq, getWindowControl(), scItemBuilder);
		listenTo(itemEditor);
		scoreEditor = new ChoiceScoreController(ureq, getWindowControl(), scItemBuilder);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbackEditorController(ureq, getWindowControl(), scItemBuilder, false, true, true);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.choice"), itemEditor.getInitialComponent());
		tabbedPane.addTab(translate("form.score"), scoreEditor.getInitialComponent());
		tabbedPane.addTab(translate("form.feedback"), feedbackEditor.getInitialComponent());
		return scItemBuilder;
	}
	
	private AssessmentItemBuilder initMultipleChoiceEditors(UserRequest ureq, AssessmentItem item) {
		MultipleChoiceAssessmentItemBuilder mcItemBuilder = new MultipleChoiceAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new MultipleChoiceEditorController(ureq, getWindowControl(), mcItemBuilder);
		listenTo(itemEditor);
		scoreEditor = new ChoiceScoreController(ureq, getWindowControl(), mcItemBuilder);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbackEditorController(ureq, getWindowControl(), mcItemBuilder, false, true, true);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.choice"), itemEditor.getInitialComponent());
		tabbedPane.addTab(translate("form.score"), scoreEditor.getInitialComponent());
		tabbedPane.addTab(translate("form.feedback"), feedbackEditor.getInitialComponent());
		return mcItemBuilder;
	}
	
	private AssessmentItemBuilder initKPrimChoiceEditors(UserRequest ureq, AssessmentItem item) {
		KPrimAssessmentItemBuilder kprimItemBuilder = new KPrimAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new KPrimEditorController(ureq, getWindowControl(), kprimItemBuilder);
		listenTo(itemEditor);
		scoreEditor = new MinimalScoreController(ureq, getWindowControl(), kprimItemBuilder);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbackEditorController(ureq, getWindowControl(), kprimItemBuilder, false, true, true);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.choice"), itemEditor.getInitialComponent());
		tabbedPane.addTab(translate("form.score"), scoreEditor.getInitialComponent());
		tabbedPane.addTab(translate("form.feedback"), feedbackEditor.getInitialComponent());
		return kprimItemBuilder;
	}
	
	private AssessmentItemBuilder initEssayEditors(UserRequest ureq, AssessmentItem item) {
		EssayAssessmentItemBuilder essayItemBuilder = new EssayAssessmentItemBuilder(item, qtiService.qtiSerializer());
		itemEditor = new EssayEditorController(ureq, getWindowControl(), essayItemBuilder);
		listenTo(itemEditor);
		scoreEditor = new MinimalScoreController(ureq, getWindowControl(), essayItemBuilder);
		listenTo(scoreEditor);
		feedbackEditor = new FeedbackEditorController(ureq, getWindowControl(), essayItemBuilder, true, false, false);
		listenTo(feedbackEditor);
		
		tabbedPane.addTab(translate("form.choice"), itemEditor.getInitialComponent());
		tabbedPane.addTab(translate("form.score"), scoreEditor.getInitialComponent());
		tabbedPane.addTab(translate("form.feedback"), feedbackEditor.getInitialComponent());
		return essayItemBuilder;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof AssessmentItemEvent) {
			if(event instanceof AssessmentItemEvent) {
				AssessmentItemEvent aie = (AssessmentItemEvent)event;
				if(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED.equals(aie.getCommand())) {
					doBuildAndSaveAssessmentItem();
					fireEvent(ureq, new AssessmentItemEvent(aie.getCommand(), aie.getAssessmentItem(), itemRef));
				}
			}
		}
		super.event(ureq, source, event);
	}

	private void doBuildAndSaveAssessmentItem() {
		if(itemBuilder != null) {
			itemBuilder.build();
		}
		URI itemUri = resolvedAssessmentItem.getItemLookup().getSystemId();
		File itemFile = new File(itemUri);
		qtiService.updateAssesmentObject(itemFile, resolvedAssessmentItem);
	}
}