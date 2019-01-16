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
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.model.xml.AssessmentTestBuilder;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.editor.events.AssessmentTestEvent;
import org.olat.ims.qti21.ui.editor.events.SelectEvent.SelectionTarget;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;

/**
 * 
 * Initial date: 22.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestEditorController extends BasicController implements Activateable2 {

	private final TabbedPane tabbedPane;
	private final VelocityContainer mainVC;
	
	private AssessmentTestOptionsEditorController optionsCtrl;
	private AssessmentTestPartEditorController testPartOptionsCtrl;
	private AssessmentTestFeedbackEditorController feedbackCtrl;
	
	private final File testFile;
	private final File rootDirectory;
	private final VFSContainer rootContainer;
	
	private final boolean restrictedEdit;
	private final TestPart testPart;
	private final AssessmentTest assessmentTest;
	private final AssessmentTestBuilder testBuilder;
	
	public AssessmentTestEditorController(UserRequest ureq, WindowControl wControl,
			AssessmentTestBuilder testBuilder, TestPart testPart,
			File rootDirectory, VFSContainer rootContainer, File testFile,boolean restrictedEdit) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale()));
		this.testBuilder = testBuilder;
		this.testPart = testPart;
		this.assessmentTest = testBuilder.getAssessmentTest();
		this.testFile = testFile;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.restrictedEdit = restrictedEdit;
		
		mainVC = createVelocityContainer("assessment_test_editor");
		mainVC.contextPut("restrictedEdit", restrictedEdit);
		tabbedPane = new TabbedPane("testTabs", getLocale());
		tabbedPane.addListener(this);
		mainVC.put("tabbedpane", tabbedPane);
		
		initTestEditor(ureq);
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void initTestEditor(UserRequest ureq) {
		if(testPart != null) {//combined test and single part editor
			optionsCtrl = new AssessmentTestOptionsEditorController(ureq, getWindowControl(), assessmentTest, testBuilder, restrictedEdit);
			testPartOptionsCtrl = new AssessmentTestPartEditorController(ureq, getWindowControl(), testPart, restrictedEdit, testBuilder.isEditable());
			testPartOptionsCtrl.setFormTitle(null);
			listenTo(testPartOptionsCtrl);
		} else {
			optionsCtrl = new AssessmentTestOptionsEditorController(ureq, getWindowControl(), assessmentTest, testBuilder, restrictedEdit);
		}
		listenTo(optionsCtrl);
		
		feedbackCtrl = new AssessmentTestFeedbackEditorController(ureq, getWindowControl(), testBuilder,
				rootDirectory, rootContainer, testFile, restrictedEdit);
		listenTo(feedbackCtrl);
		
		tabbedPane.addTab(translate("assessment.test.config"), optionsCtrl.getInitialComponent());
		if(testBuilder.isEditable()) {
			tabbedPane.addTab(translate("form.feedback"), feedbackCtrl.getInitialComponent());
		}
		if(testPartOptionsCtrl != null) {
			tabbedPane.addTab(translate("assessment.test.expert.config"), testPartOptionsCtrl.getInitialComponent());
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(SelectionTarget.description.name().equalsIgnoreCase(type) || SelectionTarget.score.name().equalsIgnoreCase(type)) {
			activate(ureq, optionsCtrl);
		} else if(SelectionTarget.expert.name().equalsIgnoreCase(type) && testPartOptionsCtrl != null) {
			activate(ureq, testPartOptionsCtrl);
		} else if(SelectionTarget.feedback.name().equalsIgnoreCase(type) && testPartOptionsCtrl != null) {
			activate(ureq, testPartOptionsCtrl);
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
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(optionsCtrl == source || feedbackCtrl == source || testPartOptionsCtrl == source) {
			if(AssessmentTestEvent.ASSESSMENT_TEST_CHANGED_EVENT.equals(event)) {
				testBuilder.build();
				if(optionsCtrl == source) {
					feedbackCtrl.sync();
				}
				fireEvent(ureq, event);
			}
		}
	}
}
