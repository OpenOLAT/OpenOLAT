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
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.editor.events.AssessmentSectionEvent;
import org.olat.ims.qti21.ui.editor.events.OpenTestConfigurationOverviewEvent;
import org.olat.ims.qti21.ui.editor.events.SelectEvent.SelectionTarget;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 22.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentSectionEditorController extends BasicController implements Activateable2 {

	private final TabbedPane tabbedPane;
	private final VelocityContainer mainVC;
	
	private final File testFile;
	private final File rootDirectory;
	private final VFSContainer rootContainer;
	
	private final AssessmentSection section;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	
	private final boolean editable;
	private final boolean restrictedEdit;
	
	private AssessmentSectionOptionsEditorController optionsCtrl;
	private AssessmentSectionExpertOptionsEditorController expertOptionsCtrl;
	
	public AssessmentSectionEditorController(UserRequest ureq, WindowControl wControl,
			AssessmentSection section, ResolvedAssessmentTest resolvedAssessmentTest,
			File rootDirectory, VFSContainer rootContainer, File testFile,
			boolean restrictedEdit, boolean editable) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale()));
		this.section = section;
		this.editable = editable;
		this.testFile = testFile;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.restrictedEdit = restrictedEdit;
		this.resolvedAssessmentTest = resolvedAssessmentTest;
		
		mainVC = createVelocityContainer("assessment_test_editor");
		mainVC.contextPut("restrictedEdit", restrictedEdit);
		tabbedPane = new TabbedPane("testTabs", getLocale());
		tabbedPane.setElementCssClass("o_sel_assessment_section_config");
		tabbedPane.addListener(this);
		mainVC.put("tabbedpane", tabbedPane);
		
		initSectionEditor(ureq);
		putInitialPanel(mainVC);
	}

	private void initSectionEditor(UserRequest ureq) {
		optionsCtrl = new AssessmentSectionOptionsEditorController(ureq, getWindowControl(),
				section, resolvedAssessmentTest, rootDirectory, rootContainer, testFile, restrictedEdit, editable);
		listenTo(optionsCtrl);
		expertOptionsCtrl = new AssessmentSectionExpertOptionsEditorController(ureq, getWindowControl(), section, restrictedEdit, editable);
		listenTo(expertOptionsCtrl);
		tabbedPane.addTab(translate("assessment.section.config"), optionsCtrl.getInitialComponent());
		tabbedPane.addTab(translate("assessment.section.expert.config"), expertOptionsCtrl.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(SelectionTarget.description.name().equalsIgnoreCase(type)) {
			tabbedPane.setSelectedPane(ureq, tabbedPane.indexOfTab(optionsCtrl.getInitialComponent()));
		} else if(SelectionTarget.expert.name().equalsIgnoreCase(type)
				|| SelectionTarget.attempts.name().equalsIgnoreCase(type)) {
			tabbedPane.setSelectedPane(ureq, tabbedPane.indexOfTab(expertOptionsCtrl.getInitialComponent()));
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(optionsCtrl == source || expertOptionsCtrl == source) {
			if(AssessmentSectionEvent.ASSESSMENT_SECTION_CHANGED.equals(event.getCommand())
					|| OpenTestConfigurationOverviewEvent.OPEN_TEST_CONFIGURATION_OVERVIEW.equals(event.getCommand())) {
				fireEvent(ureq, event);
			}
		}
	}
}
