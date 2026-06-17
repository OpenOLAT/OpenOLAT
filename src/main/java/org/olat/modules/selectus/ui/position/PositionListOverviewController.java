/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.position;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.badge.Badge;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.PositionListController;
import org.olat.modules.selectus.ui.events.SelectApplicationEvent;
import org.olat.modules.selectus.ui.events.SelectPositionEvent;
import org.olat.modules.selectus.ui.events.SelectPositionLightEvent;
import org.olat.modules.selectus.ui.feedback.appsfeedback.MemberFeedbacksController;
import org.olat.modules.selectus.ui.notifications.NotificationListController;
import org.olat.modules.selectus.ui.reference.MyApplicationsListController;

/**
 * 
 * Initial date: 22 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionListOverviewController extends BasicController implements TooledController, Activateable2  {

	private final StackedPanel mainPanel;

	private Link feedbackButton;
	private final Link positionLogButton;
	private final Link positionListButton;
	private final Link myApplicationListButton;
	private final TooledStackedPanel stackPanel;
	private final ButtonGroupComponent segmentButtonsCmp;
	
	private final boolean hasFeedbacks;
	private final boolean hasPositions;
	private final boolean hasApplications;
	
	private MemberFeedbacksController feedbacksCtrl;
	private final PositionListController positionListCtrl;
	private final NotificationListController notificationListCtrl;
	private final MyApplicationsListController myApplicationsCtrl;
	
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionListOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RecruitingSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		
		this.stackPanel = stackPanel;
		
		positionListCtrl = new PositionListController(ureq, wControl, stackPanel, secCallback);
		listenTo(positionListCtrl);
		myApplicationsCtrl = new MyApplicationsListController(ureq, wControl, stackPanel);
		listenTo(myApplicationsCtrl);
		
		hasPositions = positionListCtrl.getNumOfPositions() > 0;
		hasFeedbacks = recruitingModule.isMembersFeedbackEnabled()
				&& feedbackService.hasApplicationFeedbacks(getIdentity());
		hasApplications = recruitingModule.isReferenceApplicantManagement()
				&& recruitingService.hasApplicationByIdentity(getIdentity());

		segmentButtonsCmp = new ButtonGroupComponent("segments");
		positionListButton = LinkFactory.createToolLink("position.list", translate("position.list"), this);
		if(hasPositions && !ureq.getUserSession().getRoles().isInvitee()) {
			segmentButtonsCmp.addButton(positionListButton, true);
		}
		
		myApplicationListButton = LinkFactory.createToolLink("my.applications.list", translate("my.applications.list"), this);
		if(hasApplications) {
			segmentButtonsCmp.addButton(myApplicationListButton, false);
		}
		
		feedbackButton = LinkFactory.createToolLink("member.feedbacks", translate("member.feedbacks"), this);
		if(hasFeedbacks) {
			segmentButtonsCmp.addButton(feedbackButton, !hasPositions);
		}
		
		positionLogButton = LinkFactory.createToolLink("position.list.log", translate("position.list.log"), this);
		if(hasPositions || hasFeedbacks) {
			segmentButtonsCmp.addButton(positionLogButton, false);
		}

		mainPanel = new SimpleStackedPanel("positionsOverview");
		putInitialPanel(mainPanel);

		WindowControl sfwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Feedbacks"), null);
		feedbacksCtrl = new MemberFeedbacksController(ureq, sfwControl, stackPanel);
		listenTo(feedbacksCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Logs"), null);
		notificationListCtrl = new NotificationListController(ureq, swControl);
		listenTo(notificationListCtrl);
		
		if(hasFeedbacks && !hasPositions) {
			segmentButtonsCmp.setSelectedButton(feedbackButton);
			mainPanel.setContent(feedbacksCtrl.getInitialComponent());
		} else if(hasApplications && !hasPositions) {
			myApplicationsCtrl.loadModel();
			segmentButtonsCmp.setSelectedButton(myApplicationListButton);
			mainPanel.setContent(myApplicationsCtrl.getInitialComponent());
			
			getWindowControl().getWindowBackOffice().getChiefController().addBodyCssClass("o_referee_dashboard_only");
		} else {

			segmentButtonsCmp.setSelectedButton(positionListButton);
			mainPanel.setContent(positionListCtrl.getInitialComponent());
		}
		
		loadUnreadNotificationsBadge(false);
	}
	
	public void loadUnreadNotificationsBadge(boolean unloadModel) {
		if(!recruitingModule.isNotificationsToolEnabled()) return;
		
		int numOfUnreadNotifications = notificationListCtrl.getNumOfUnreadNotifications();
		if(numOfUnreadNotifications > 0) {
			positionLogButton.setBadge(Integer.toString(numOfUnreadNotifications), Badge.Level.none);
		} else {
			positionLogButton.removeBadge();
		}
		
		if(unloadModel) {
			loadModel();
		}
	}
	
	public void loadModel() {
		if(notificationListCtrl != null) {
			notificationListCtrl.loadModel();
		}
	}
	
	public int getNumOfPositions() {
		return positionListCtrl.getNumOfPositions();
	}
	
	public int getNumOfApplications() {
		return myApplicationsCtrl.getNumOfApplications();
	}
	
	public boolean hasFeedbacks() {
		return hasFeedbacks;
	}
	
	public Long getPositionKeyAt(int index) {
		return positionListCtrl.getPositionKeyAt(index);
	}
	
	public boolean hasPositionWith(Long key) {
		return positionListCtrl.hasPositionWith(key);
	}

	@Override
	public void initTools() {
		positionListCtrl.initTools();
		if(recruitingModule.isNotificationsToolEnabled()) {
			stackPanel.addTool(segmentButtonsCmp, Align.segment);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		removeModalControllers();
		if(entries == null || entries.isEmpty()) {
			if(segmentButtonsCmp.getSelectedButton() == myApplicationListButton
					&& myApplicationsCtrl.getNumOfApplications() == 1) {
				myApplicationsCtrl.openFirstApplication(ureq);
			}
		} else {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Logs".equals(type)) {
				if(recruitingModule.isNotificationsToolEnabled()) {
					segmentButtonsCmp.setSelectedButton(positionLogButton);
					doOpenPositionLog(ureq);
				}
			} else if("Feedbacks".equals(type)) {
				if(hasFeedbacks) {
					segmentButtonsCmp.setSelectedButton(feedbackButton);
					doOpenFeedbacks(ureq);
				}
			} else if("MyApplication".equals(type) || "MyApplications".equals(type)) {
				doOpenMyApplications(ureq);
				segmentButtonsCmp.setSelectedButton(myApplicationListButton);
				if(myApplicationsCtrl.getNumOfApplications() == 1) {
					myApplicationsCtrl.openFirstApplication(ureq);
				}  else {
					myApplicationsCtrl.activate(ureq, entries, state);
				}
			}
		}
	}
	
	@Override
	public void removeModalControllers() {
		if(this.positionListCtrl != null) {
			positionListCtrl.removeModalControllers();
		}
		super.removeModalControllers();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(positionLogButton == source) {
			segmentButtonsCmp.setSelectedButton(positionLogButton);
			doOpenPositionLog(ureq);
		} else if(positionListButton == source) {
			segmentButtonsCmp.setSelectedButton(positionListButton);
			doOpenPositionList(ureq);
		} else if(feedbackButton == source) {
			segmentButtonsCmp.setSelectedButton(feedbackButton);
			doOpenFeedbacks(ureq);
		} else if(myApplicationListButton == source) {
			segmentButtonsCmp.setSelectedButton(myApplicationListButton);
			doOpenMyApplications(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof SelectApplicationEvent || event instanceof SelectPositionLightEvent
				|| event instanceof SelectPositionEvent || event == Event.CHANGED_EVENT) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	public void reload() {
		positionListCtrl.loadModel();
		loadUnreadNotificationsBadge(false);
	}
	
	private void doOpenPositionList(UserRequest ureq) {
		mainPanel.setContent(positionListCtrl.getInitialComponent());
		addToHistory(ureq, positionListCtrl);
		loadUnreadNotificationsBadge(true);
	}
	
	private void doOpenPositionLog(UserRequest ureq) {
		notificationListCtrl.loadModel();
		addToHistory(ureq, notificationListCtrl);
		mainPanel.setContent(notificationListCtrl.getInitialComponent());
		loadUnreadNotificationsBadge(false);
	}
	
	private void doOpenFeedbacks(UserRequest ureq) {
		feedbacksCtrl.loadModel();
		addToHistory(ureq, feedbacksCtrl);
		mainPanel.setContent(feedbacksCtrl.getInitialComponent());
	}
	
	private void doOpenMyApplications(UserRequest ureq) {
		myApplicationsCtrl.loadModel();
		addToHistory(ureq, myApplicationsCtrl);
		mainPanel.setContent(myApplicationsCtrl.getInitialComponent());
	}
}
