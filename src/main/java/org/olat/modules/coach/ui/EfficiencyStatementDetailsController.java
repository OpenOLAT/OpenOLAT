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
package org.olat.modules.coach.ui;

import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.IdentityAssessmentEditController;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementController;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  9 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EfficiencyStatementDetailsController extends BasicController implements Activateable2 {
	
	private final VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	private Link assessmentLink,  efficiencyStatementLink;
	
	private boolean hasChanged;
	private EfficiencyStatementEntry statementEntry;
	private CertificateAndEfficiencyStatementController statementCtrl;
	private IdentityAssessmentEditController assessmentCtrl;
	
	private final Identity assessedIdentity;
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;
	
	public EfficiencyStatementDetailsController(UserRequest ureq, WindowControl wControl,
			EfficiencyStatementEntry statementEntry, boolean selectAssessmentTool) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("efficiency_details");
		this.statementEntry = statementEntry;

		RepositoryEntry entry = statementEntry.getCourse();
		assessedIdentity = securityManager.loadIdentityByKey(statementEntry.getStudentKey());
		statementCtrl = createEfficiencyStatementController(ureq);
		listenTo(statementCtrl);
		
		if(entry == null) {
			mainVC.put("segmentCmp", statementCtrl.getInitialComponent());
		} else {
			try {
				ICourse course = CourseFactory.loadCourse(entry.getOlatResource());
				assessmentCtrl = new IdentityAssessmentEditController(wControl, ureq, null,
						assessedIdentity, course, true, false, false);
				listenTo(assessmentCtrl);
				
				segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
				efficiencyStatementLink = LinkFactory.createLink("details.statement", mainVC, this);
				segmentView.addSegment(efficiencyStatementLink, !selectAssessmentTool);
				
				assessmentLink = LinkFactory.createLink("details.assessment", mainVC, this);
				segmentView.addSegment(assessmentLink, selectAssessmentTool);
				
				if(selectAssessmentTool) {
					mainVC.put("segmentCmp", assessmentCtrl.getInitialComponent());
				} else {
					mainVC.put("segmentCmp", statementCtrl.getInitialComponent());
				}
			} catch(CorruptedCourseException e) {
				logError("", e);
			}
		}

		putInitialPanel(mainVC);
	}
	
	public EfficiencyStatementEntry getEntry() {
		return statementEntry;
	}
	
	public boolean isAssessmentToolSelected() {
		return assessmentCtrl != null && assessmentCtrl.getInitialComponent() == mainVC.getComponent("segmentCmp"); 
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(assessmentCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				//reload the details
				efficiencyStatementChanged();
				hasChanged = true;
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView && event instanceof SegmentViewEvent) {
			SegmentViewEvent sve = (SegmentViewEvent)event;
			if(efficiencyStatementLink != null && efficiencyStatementLink.getComponentName().equals(sve.getComponentName())) {
				if(hasChanged) {
					//reload
					removeAsListenerAndDispose(statementCtrl);
					statementCtrl = createEfficiencyStatementController(ureq);
					listenTo(statementCtrl);
					hasChanged = false;
				}
				mainVC.put("segmentCmp", statementCtrl.getInitialComponent());
			} else if(assessmentLink != null && assessmentLink.getComponentName().equals(sve.getComponentName())) {
				mainVC.put("segmentCmp", assessmentCtrl.getInitialComponent());
			}
		}
	}
	
	private CertificateAndEfficiencyStatementController createEfficiencyStatementController(UserRequest ureq) {
		RepositoryEntry entry = statementEntry.getCourse();
		UserEfficiencyStatement statement = statementEntry.getUserEfficencyStatement();
		EfficiencyStatement efficiencyStatement = null;
		if(statement != null) {
			efficiencyStatement = efficiencyStatementManager.getUserEfficiencyStatementByCourseRepoKey(statement.getCourseRepoKey(), assessedIdentity);
		}
		return new CertificateAndEfficiencyStatementController(getWindowControl(), ureq, assessedIdentity, null, entry.getOlatResource().getKey(), entry, efficiencyStatement, true);
	}
	
	private void efficiencyStatementChanged() {
		List<Identity> assessedIdentityList = Collections.singletonList(assessedIdentity);
		RepositoryEntry re = statementEntry.getCourse();
		efficiencyStatementManager.updateEfficiencyStatements(re.getOlatResource(), assessedIdentityList);
	}
}