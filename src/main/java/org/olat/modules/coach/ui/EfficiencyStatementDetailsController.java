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
import org.olat.core.CoreSpringFactory;
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
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.EfficiencyStatementController;
import org.olat.course.assessment.EfficiencyStatementManager;
import org.olat.course.assessment.IdentityAssessmentEditController;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.group.BusinessGroup;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.repository.RepositoryEntry;

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
	private final SegmentViewComponent segmentView;
	private final Link efficiencyStatementLink, assessmentLink;
	
	private boolean hasChanged;
	private EfficiencyStatementEntry statementEntry;
	private EfficiencyStatementController statementCtrl;
	private IdentityAssessmentEditController assessmentCtrl;
	
	private final Identity assessedIdentity;
	
	private final BusinessGroup group;
	private final BaseSecurity securityManager;
	private final EfficiencyStatementManager efficiencyStatementManager;
	
	public EfficiencyStatementDetailsController(UserRequest ureq, WindowControl wControl,
			EfficiencyStatementEntry statementEntry, BusinessGroup group) {
		super(ureq, wControl);
		
		efficiencyStatementManager = EfficiencyStatementManager.getInstance();
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);

		mainVC = createVelocityContainer("efficiency_details");
		this.group = group;
		this.statementEntry = statementEntry;

		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		efficiencyStatementLink = LinkFactory.createLink("details.statement", mainVC, this);
		segmentView.addSegment(efficiencyStatementLink, true);
		
		assessmentLink = LinkFactory.createLink("details.assessment", mainVC, this);
		segmentView.addSegment(assessmentLink, false);

		RepositoryEntry entry = statementEntry.getCourse();
		assessedIdentity = securityManager.loadIdentityByKey(statementEntry.getStudentKey());
		statementCtrl = createEfficiencyStatementController(ureq);
		listenTo(statementCtrl);
		
		if(entry == null) {
			assessmentLink.setEnabled(false);
		} else {
			ICourse course = CourseFactory.loadCourse(entry.getOlatResource());
			assessmentCtrl = new IdentityAssessmentEditController(wControl, ureq, null, assessedIdentity, course, true, false);
			listenTo(assessmentCtrl);
		}

		mainVC.put("segmentCmp", statementCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}
	
	public EfficiencyStatementEntry getEntry() {
		return statementEntry;
	}
	
	@Override
	protected void doDispose() {
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
			if(efficiencyStatementLink.getComponentName().equals(sve.getComponentName())) {
				if(hasChanged) {
					//reload
					removeAsListenerAndDispose(statementCtrl);
					statementCtrl = createEfficiencyStatementController(ureq);
					listenTo(statementCtrl);
					hasChanged = false;
				}
				mainVC.put("segmentCmp", statementCtrl.getInitialComponent());
			} else if(assessmentLink.getComponentName().equals(sve.getComponentName())) {
				mainVC.put("segmentCmp", assessmentCtrl.getInitialComponent());
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
	
	private EfficiencyStatementController createEfficiencyStatementController(UserRequest ureq) {
		RepositoryEntry entry = statementEntry.getCourse();
		UserEfficiencyStatement statement = statementEntry.getUserEfficencyStatement();
		EfficiencyStatement efficiencyStatement = null;
		if(statement != null) {
			efficiencyStatement = EfficiencyStatementManager.getInstance().getUserEfficiencyStatement(statement.getCourseRepoKey(), assessedIdentity);
		}
		return new EfficiencyStatementController(getWindowControl(), ureq, assessedIdentity, group, entry, efficiencyStatement, true);
	}
	
	private void efficiencyStatementChanged() {
		Identity assessedIdentity = securityManager.loadIdentityByKey(statementEntry.getStudentKey());
		List<Identity> assessedIdentityList = Collections.singletonList(assessedIdentity);
		RepositoryEntry re = statementEntry.getCourse();
		efficiencyStatementManager.updateEfficiencyStatements(re.getOlatResource(), assessedIdentityList);
	}
}