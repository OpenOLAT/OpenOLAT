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
package org.olat.course.assessment.ui.mode;

import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.group.BusinessGroup;
import org.olat.group.area.BGArea;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21 janv. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeEditController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final TabbedPane tabbedPane;
	
	private AssessmentModeEditAccessController accessCtrl;
	private AssessmentModeEditGeneralController generalCtrl;
	private AssessmentModeEditRestrictionController restrictionCtrl;
	private AssessmentModeEditSafeExamBrowserController safeExamBrowserCtrl;
	
	private RepositoryEntry entry;
	private AssessmentMode assessmentMode;
	private boolean create;
	private Set<BGArea> assessmentModeAreas;
	private Set<BusinessGroup> assessmentModeBusinessGroups;
	private Set<CurriculumElement> assessmentModeCurriculumElements;

	public AssessmentModeEditController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, AssessmentMode assessmentMode) {
		super(ureq, wControl);
		this.entry = entry;
		this.assessmentMode = assessmentMode;
		this.create = assessmentMode.getKey() == null;
		
		mainVC = createVelocityContainer("edit");
		tabbedPane = new TabbedPane("segments", getLocale());
		mainVC.put("segments", tabbedPane);

		tabbedPane.addTabControllerCreator(ureq, translate("tab.edit.general"), null, uureq -> {
			generalCtrl = new AssessmentModeEditGeneralController(uureq, getWindowControl(), entry, assessmentMode);
			listenTo(generalCtrl);
			return generalCtrl;
		}, true);
		
		tabbedPane.addTabControllerCreator(ureq, translate("tab.edit.restriction"), null, uureq -> {
			restrictionCtrl = new AssessmentModeEditRestrictionController(uureq, getWindowControl(), entry, assessmentMode);
			listenTo(restrictionCtrl);
			return restrictionCtrl;
		}, true);
		
		tabbedPane.addTabControllerCreator(ureq, translate("tab.edit.access"), null, uureq -> {
			accessCtrl = new AssessmentModeEditAccessController(uureq, getWindowControl(), entry, assessmentMode);
			if(assessmentModeAreas != null) {
				accessCtrl.selectAreas(assessmentModeAreas);
			}
			if(assessmentModeBusinessGroups != null) {
				accessCtrl.selectBusinessGroups(assessmentModeBusinessGroups);
			}
			if(assessmentModeCurriculumElements != null) {
				accessCtrl.selectCurriculumElements(assessmentModeCurriculumElements);
			}
			listenTo(accessCtrl);
			return accessCtrl;
		}, true);
		
		tabbedPane.addTabControllerCreator(ureq, translate("tab.edit.seb"), null, uureq -> {
			safeExamBrowserCtrl = new AssessmentModeEditSafeExamBrowserController(uureq, getWindowControl(), entry, assessmentMode);
			listenTo(safeExamBrowserCtrl);
			return safeExamBrowserCtrl;
		}, true);
		
		putInitialPanel(mainVC);
		updateSegments();
		updateTitle();
	}
	
	private void updateTitle() {
		if(StringHelper.containsNonWhitespace(assessmentMode.getName())) {
			mainVC.contextPut("title", translate("form.mode.title", assessmentMode.getName()));
		} else {
			mainVC.contextPut("title", translate("form.mode.title.add"));
		}
	}
	
	private void updateSegments() {
		boolean persisted = assessmentMode != null && assessmentMode.getKey() != null;
		tabbedPane.setEnabled(1, persisted);
		tabbedPane.setEnabled(2, persisted);
		tabbedPane.setEnabled(3, persisted);
	}
	
	protected void setBusinessGroups(Set<BusinessGroup> assessmentModeBusinessGroups) {
		this.assessmentModeBusinessGroups = assessmentModeBusinessGroups;
	}
	
	protected void setAreas(Set<BGArea> assessmentModeToAreas) {
		this.assessmentModeAreas = assessmentModeToAreas;
	}
	
	protected void setCurriculumElements(Set<CurriculumElement> assessmentModeToCurriculumElements) {
		this.assessmentModeCurriculumElements = assessmentModeToCurriculumElements;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == generalCtrl) {
			if(event == Event.CHANGED_EVENT) {
				assessmentMode = generalCtrl.getAssessmentMode();
				updateSegments();
				updateTitle();
				if (create) {
					getOrCreateAccessCtrl(ureq).saveRelations(assessmentMode.getTargetAudience());
					create = false;
				}
			}
		} else if(source == restrictionCtrl) {
			if(event == Event.CHANGED_EVENT) {
				assessmentMode = restrictionCtrl.getAssessmentMode();
			}
		} else if(source == accessCtrl) {
			if(event == Event.CHANGED_EVENT) {
				assessmentMode = accessCtrl.getAssessmentMode();
			}
		} else if(source == safeExamBrowserCtrl) {
			if(event == Event.CHANGED_EVENT) {
				assessmentMode = safeExamBrowserCtrl.getAssessmentMode();
			}
		}
		super.event(ureq, source, event);
	}
	
	private AssessmentModeEditAccessController getOrCreateAccessCtrl(UserRequest ureq) {
		if(accessCtrl == null) {
			accessCtrl = new AssessmentModeEditAccessController(ureq, getWindowControl(), entry, assessmentMode);
			if(assessmentModeAreas != null) {
				accessCtrl.selectAreas(assessmentModeAreas);
			}
			if(assessmentModeBusinessGroups != null) {
				accessCtrl.selectBusinessGroups(assessmentModeBusinessGroups);
			}
			if(assessmentModeCurriculumElements != null) {
				accessCtrl.selectCurriculumElements(assessmentModeCurriculumElements);
			}
			listenTo(accessCtrl);
		}
		return accessCtrl;
	}
}