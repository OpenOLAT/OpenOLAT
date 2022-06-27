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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
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
	private SegmentViewComponent segmentView;
	private Link generalLink;
	private Link restrictionLink;
	private Link accessLink;
	private Link safeExamBrowserLink;
	
	private AssessmentModeEditAccessController accessCtrl;
	private AssessmentModeEditGeneralController generalCtrl;
	private AssessmentModeEditRestrictionController restrictionCtrl;
	private AssessmentModeEditSafeExamBrowserController safeExamBrowserCtrl;
	
	private RepositoryEntry entry;
	private AssessmentMode assessmentMode;
	boolean create;
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
		if(StringHelper.containsNonWhitespace(assessmentMode.getName())) {
			mainVC.contextPut("title", translate("form.mode.title", assessmentMode.getName()));
		} else {
			mainVC.contextPut("title", translate("form.mode.title.add"));
		}
		
		tabbedPane = new TabbedPane("segments", getLocale());
		mainVC.put("segments", tabbedPane);
		generalCtrl = new AssessmentModeEditGeneralController(ureq, getWindowControl(), entry, assessmentMode);
		listenTo(generalCtrl);
		tabbedPane.addTab(translate("tab.edit.general"), generalCtrl);
		
		tabbedPane.addTabControllerCreator(ureq, translate("tab.edit.restriction"), uureq -> {
			restrictionCtrl = new AssessmentModeEditRestrictionController(uureq, getWindowControl(), entry, assessmentMode);
			listenTo(restrictionCtrl);
			return restrictionCtrl;
		});
		
		tabbedPane.addTabControllerCreator(ureq, translate("tab.edit.access"), uureq -> {
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
		});
		
		tabbedPane.addTabControllerCreator(ureq, translate("tab.edit.seb"), uureq -> {
			safeExamBrowserCtrl = new AssessmentModeEditSafeExamBrowserController(uureq, getWindowControl(), entry, assessmentMode);
			listenTo(safeExamBrowserCtrl);
			return safeExamBrowserCtrl;
		});
		
		putInitialPanel(mainVC);
		updateSegments();
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
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == generalLink) {
					doOpenGeneral(ureq);
				} else if (clickedLink == restrictionLink){
					doOpenRestriction(ureq);
				} else if (clickedLink == accessLink){
					doOpenAccess(ureq);
				} else if (clickedLink == safeExamBrowserLink){
					doOpenSafeExamBrowser(ureq);
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == generalCtrl) {
			if(event == Event.CHANGED_EVENT) {
				assessmentMode = generalCtrl.getAssessmentMode();
				updateSegments();
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
	
	private void doOpenGeneral(UserRequest ureq) {
		if(generalCtrl == null) {
			generalCtrl = new AssessmentModeEditGeneralController(ureq, getWindowControl(), entry, assessmentMode);
			listenTo(generalCtrl);
		}
		addToHistory(ureq, generalCtrl);
		mainVC.put("segmentCmp", generalCtrl.getInitialComponent());
	}
	
	private void doOpenRestriction(UserRequest ureq) {
		if(restrictionCtrl == null) {
			restrictionCtrl = new AssessmentModeEditRestrictionController(ureq, getWindowControl(), entry, assessmentMode);
			listenTo(restrictionCtrl);
		}
		addToHistory(ureq, restrictionCtrl);
		mainVC.put("segmentCmp", restrictionCtrl.getInitialComponent());
	}
	
	private void doOpenAccess(UserRequest ureq) {
		getOrCreateAccessCtrl(ureq);
		addToHistory(ureq, accessCtrl);
		mainVC.put("segmentCmp", accessCtrl.getInitialComponent());
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
	
	private void doOpenSafeExamBrowser(UserRequest ureq) {
		if(safeExamBrowserCtrl == null) {
			safeExamBrowserCtrl = new AssessmentModeEditSafeExamBrowserController(ureq, getWindowControl(), entry, assessmentMode);
			listenTo(safeExamBrowserCtrl);
		}
		addToHistory(ureq, safeExamBrowserCtrl);
		mainVC.put("segmentCmp", safeExamBrowserCtrl.getInitialComponent());
	}
}