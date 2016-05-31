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
package org.olat.course.nodes.gta.ui;

import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.ui.events.SelectBusinessGroupEvent;
import org.olat.course.nodes.gta.ui.events.SelectIdentityEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachSelectionController extends BasicController {

	private GTACoachController coachingCtrl;
	private GTACoachedGroupListController groupListCtrl;
	private GTACoachedParticipantListController participantListCtrl;
	
	private final Link backLink;
	private final VelocityContainer mainVC;
	
	private final GTACourseNode gtaNode;
	private final CourseEnvironment courseEnv;
	
	protected final PublisherData publisherData;
	protected final SubscriptionContext subsContext;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private BaseSecurity securityManager;
	
	public GTACoachSelectionController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl);
		this.gtaNode = gtaNode;
		this.courseEnv = coachCourseEnv.getCourseEnvironment();
		
		mainVC = createVelocityContainer("coach_selection");
		backLink = LinkFactory.createLinkBack(mainVC, this);
		
		publisherData = gtaManager.getPublisherData(courseEnv, gtaNode);
		subsContext = gtaManager.getSubscriptionContext(courseEnv, gtaNode);
		if (subsContext != null) {
			ContextualSubscriptionController contextualSubscriptionCtr = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, publisherData);
			listenTo(contextualSubscriptionCtr);
			mainVC.put("contextualSubscription", contextualSubscriptionCtr.getInitialComponent());
		}
		
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			List<BusinessGroup> groups;
			CourseGroupManager gm = coachCourseEnv.getCourseEnvironment().getCourseGroupManager();
			if(coachCourseEnv.isAdmin()) {
				groups = gm.getAllBusinessGroups();
			} else if (coachCourseEnv instanceof UserCourseEnvironmentImpl) {
				groups = ((UserCourseEnvironmentImpl)coachCourseEnv).getCoachedGroups();
			} else {
				groups = Collections.emptyList();
			}
			
			groups = gtaManager.filterBusinessGroups(groups, gtaNode);
			
			if(groups.size() == 1) {
				doSelectBusinessGroup(ureq, groups.get(0));
			} else {
				groupListCtrl = new GTACoachedGroupListController(ureq, getWindowControl(), courseEnv, gtaNode, groups);
				listenTo(groupListCtrl);
				mainVC.put("list", groupListCtrl.getInitialComponent());
			}	
		} else {
			participantListCtrl = new GTACoachedParticipantListController(ureq, getWindowControl(), coachCourseEnv, gtaNode);
			listenTo(participantListCtrl);
			mainVC.put("list", participantListCtrl.getInitialComponent());
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(groupListCtrl == source) {
			if(event instanceof SelectBusinessGroupEvent) {
				SelectBusinessGroupEvent selectEvent = (SelectBusinessGroupEvent)event;
				doSelectBusinessGroup(ureq, selectEvent.getBusinessGroup());
				backLink.setVisible(true);
			}
		} else if(participantListCtrl == source) {
			if(event instanceof SelectIdentityEvent) {
				SelectIdentityEvent selectEvent = (SelectIdentityEvent)event;
				Identity selectedIdentity = securityManager.loadIdentityByKey(selectEvent.getIdentityKey());
				doSelectParticipant(ureq, selectedIdentity);
				backLink.setVisible(true);
			}
			
		}
		
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(backLink == source) {
			back();
		}
	}
	
	private void back() {
		if(coachingCtrl != null) {
			mainVC.remove(coachingCtrl.getInitialComponent());
			removeAsListenerAndDispose(coachingCtrl);
			coachingCtrl = null;
		}
		backLink.setVisible(false);
		if (participantListCtrl != null) {
			participantListCtrl.updateModel();			
		} 
		if (groupListCtrl != null) {
			groupListCtrl.updateModel();
			
		}
	}
	
	private void doSelectBusinessGroup(UserRequest ureq, BusinessGroup group) {
		removeAsListenerAndDispose(coachingCtrl);
		coachingCtrl = new GTACoachController(ureq, getWindowControl(), courseEnv, gtaNode, group, true, true, false);
		listenTo(coachingCtrl);
		mainVC.put("selection", coachingCtrl.getInitialComponent());
	}
	
	private void doSelectParticipant(UserRequest ureq, Identity identity) {
		removeAsListenerAndDispose(coachingCtrl);
		coachingCtrl = new GTACoachController(ureq, getWindowControl(), courseEnv, gtaNode, identity, true, true, false);
		listenTo(coachingCtrl);
		mainVC.put("selection", coachingCtrl.getInitialComponent());
	}
}
