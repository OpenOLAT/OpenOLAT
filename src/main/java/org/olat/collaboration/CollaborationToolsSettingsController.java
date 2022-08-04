/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.collaboration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.ui.events.CalendarGUIModifiedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonCollaborationSettingsController;
import org.olat.modules.teams.ui.TeamsCollaborationSettingsController;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomModule;
import org.olat.modules.zoom.ui.ZoomUpdateConfigController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <BR>
 * Administrative controller which allows configuration of collaboration tools.
 * <P>
 * Initial Date: Aug 23, 2004 
 * @author patrick
 */

public class CollaborationToolsSettingsController extends BasicController {

	private VelocityContainer vc_collabtools;
	private ChoiceOfToolsForm cots;
	private NewsFormController newsController;
	private CalendarToolSettingsController calendarForm;
	private FolderToolSettingsController folderForm;
	private TeamsCollaborationSettingsController teamsController; 
	private BigBlueButtonCollaborationSettingsController bigBlueButtonController;
	private ZoomUpdateConfigController zoomController;

	private boolean lastCalendarEnabledState;
	private Controller quotaCtr;
	private final String[] availableTools; 
	private final boolean managed;
	private final BusinessGroup businessGroup;
	
	@Autowired
	private QuotaManager quotaManager;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private ZoomModule zoomModule;
	@Autowired
	private ZoomManager zoomManager;

	/**
	 * Initializes this controller.
	 *
	 * @param ureq The user request.
	 * @param wControl The window control.
	 * @param businessGroup The group.
	 */
	public CollaborationToolsSettingsController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl);
		this.businessGroup = businessGroup;
		managed = BusinessGroupManagedFlag.isManaged(businessGroup, BusinessGroupManagedFlag.tools);
		// make copy to be independent, for during lifetime of controller
		availableTools = CollaborationToolsFactory.getInstance().getAvailableTools().clone();
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		
		vc_collabtools = createVelocityContainer ("collaborationtools");

		cots = new ChoiceOfToolsForm (ureq, wControl, collabTools, availableTools);
		cots.setEnabled(!managed);
		listenTo(cots);
		vc_collabtools.put("choiceOfTools", cots.getInitialComponent());
		
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_NEWS)) {
			addNewsTool(ureq);
		} else {
			vc_collabtools.contextPut("newsToolEnabled", Boolean.FALSE);
		}
		
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_BIGBLUEBUTTON)) {
			addBigBlueButtonTool(ureq);
		} else {
			vc_collabtools.contextPut("bigBlueButtonToolEnabled", Boolean.FALSE);
		}
		
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_TEAMS)) {
			addTeamsTool(ureq);
		} else {
			vc_collabtools.contextPut("teamsToolEnabled", Boolean.FALSE);
		}
		
		if (zoomModule.isEnabledForGroupTool() && collabTools.isToolEnabled(CollaborationTools.TOOL_ZOOM)) {
			addZoomTool(ureq);
		} else {
			vc_collabtools.contextPut("zoomToolEnabled", Boolean.FALSE);
		}

		UserSession usess = ureq.getUserSession();
		
		if (usess.getRoles().isAdministrator() || usess.getRoles().isSystemAdmin()) {
			vc_collabtools.contextPut("isOlatAdmin", Boolean.TRUE);
			if(managed) {
				quotaCtr = quotaManager.getQuotaViewInstance(ureq, getWindowControl(), collabTools.getFolderRelPath());
			} else {
				quotaCtr = quotaManager.getQuotaEditorInstance(ureq, getWindowControl(), collabTools.getFolderRelPath(), true, false);
			}
			listenTo(quotaCtr);
		} else {
			vc_collabtools.contextPut("isOlatAdmin", Boolean.FALSE);
		}

		// update calendar form: only show when enabled
		if(calendarModule.isEnabled() && calendarModule.isEnableGroupCalendar()) {
			if (collabTools.isToolEnabled(CollaborationTools.TOOL_CALENDAR)) {
				lastCalendarEnabledState = true;
				vc_collabtools.contextPut("calendarToolEnabled", Boolean.TRUE);
				int iCalendarAccess = CollaborationTools.CALENDAR_ACCESS_OWNERS;
				Long lCalendarAccess = collabTools.lookupCalendarAccess();
				if (lCalendarAccess != null) iCalendarAccess = lCalendarAccess.intValue();
				calendarForm = new CalendarToolSettingsController(ureq, getWindowControl(), iCalendarAccess);
				calendarForm.setEnabled(!managed);
				listenTo(calendarForm);
				
				vc_collabtools.put("calendarform", calendarForm.getInitialComponent());
			} else {
				lastCalendarEnabledState = false;
				vc_collabtools.contextPut("calendarToolEnabled", Boolean.FALSE);
			}
		}
		
		// update quota form: only show when enabled
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
			vc_collabtools.contextPut("folderToolEnabled", Boolean.TRUE);
			if(usess.getRoles().isAdministrator() || usess.getRoles().isSystemAdmin()) {
				vc_collabtools.put("quota", quotaCtr.getInitialComponent());
			}
			vc_collabtools.contextPut("folderToolEnabled", Boolean.TRUE);
			if(folderForm != null) {
				removeAsListenerAndDispose(folderForm);
			}
			Long lFolderAccess = collabTools.lookupFolderAccess();
			int access = lFolderAccess == null ? CollaborationTools.FOLDER_ACCESS_ALL : lFolderAccess.intValue();
			folderForm = new FolderToolSettingsController(ureq, getWindowControl(), access);
			folderForm.setEnabled(!managed);
			listenTo(folderForm);
			vc_collabtools.put("folderform", folderForm.getInitialComponent());
		} else {
			vc_collabtools.contextPut("folderToolEnabled", Boolean.FALSE);
		}
		
		putInitialPanel(vc_collabtools);
	}

	private void addNewsTool(UserRequest ureq) {
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		String access = collabTools.getNewsAccessProperty();
		
		if (newsController != null) {
			removeAsListenerAndDispose(newsController);
		}
		newsController = new NewsFormController(ureq, getWindowControl(), (access == null ? "" : access));
		newsController.setEnabled(!managed);
		listenTo(newsController);
		
		vc_collabtools.contextPut("newsToolEnabled", Boolean.TRUE);
		vc_collabtools.put("newsform", newsController.getInitialComponent());
	}
	
	private void addBigBlueButtonTool(UserRequest ureq) {
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		String access = collabTools.getBigBlueButtonAccessProperty();

		removeAsListenerAndDispose(bigBlueButtonController);

		bigBlueButtonController = new BigBlueButtonCollaborationSettingsController(ureq, getWindowControl(), access);
		bigBlueButtonController.setEnabled(!managed);
		listenTo(bigBlueButtonController);
		
		vc_collabtools.contextPut("bigBlueButtonToolEnabled", Boolean.TRUE);
		vc_collabtools.put("bigbluebuttonform", bigBlueButtonController.getInitialComponent());
	}
	
	private void addTeamsTool(UserRequest ureq) {
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		String access = collabTools.getTeamsAccessProperty();

		removeAsListenerAndDispose(teamsController);

		teamsController = new TeamsCollaborationSettingsController(ureq, getWindowControl(), access);
		teamsController.setEnabled(!managed);
		listenTo(teamsController);
		
		vc_collabtools.contextPut("teamsToolEnabled", Boolean.TRUE);
		vc_collabtools.put("teamsform", teamsController.getInitialComponent());
	}

	private void addZoomTool(UserRequest ureq) {
		removeAsListenerAndDispose(zoomController);

		zoomController = new ZoomUpdateConfigController(ureq, getWindowControl(), null, null, businessGroup, ZoomManager.ApplicationType.groupTool);
		zoomController.setEnabled(!managed);
		listenTo(zoomController);

		vc_collabtools.contextPut("zoomToolEnabled", Boolean.TRUE);
		vc_collabtools.put("zoomform", zoomController.getInitialComponent());
	}

	private void removeZoomTool(UserRequest ureq) {
		removeAsListenerAndDispose(zoomController);
		vc_collabtools.contextPut("zoomToolEnabled", Boolean.FALSE);
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		
		if (source == cots && event.getCommand().equals("ONCHANGE")) {
			doToolsChange(ureq, collabTools);
		} else if (source == newsController) {
			if (event.equals(Event.DONE_EVENT)) {
				String access = newsController.getAccessPropertyValue();
				collabTools.saveNewsAccessProperty(access);
			}
		} else if (source == bigBlueButtonController) {
			if (event.equals(Event.DONE_EVENT)) {
				String access = bigBlueButtonController.getAccessPropertyValue();
				collabTools.saveBigBlueButtonAccessProperty(access);
			}
		} else if (source == calendarForm) {	
			collabTools.saveCalendarAccess(Long.valueOf(calendarForm.getCalendarAccess()));
			// notify calendar components to refresh their calendars
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(
					new CalendarGUIModifiedEvent(), OresHelper.lookupType(CalendarManager.class));
		} else if (source == folderForm) {
			collabTools.saveFolderAccess(Long.valueOf(folderForm.getFolderAccess()));
		} else if (source == zoomController) {
			collabTools.saveZoomClientId(zoomController.getClientId());
		}
	}
	
	private void doToolsChange(UserRequest ureq, CollaborationTools collabTools) {
		Collection<String> set = cots.getSelected();
		for (int i = 0; i < availableTools.length; i++) {
			// usually one should check which one changed but here
			// it is okay to set all of them because ctsm has a cache
			// and writes only when really necessary.
			collabTools.setToolEnabled(availableTools[i], set.contains(""+i));	
		}
		//reload tools after a change
		collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		
		fireEvent(ureq, Event.CHANGED_EVENT);			
		
		// update news form: only show when enabled
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_NEWS)) {
			addNewsTool(ureq);
		} else {
			vc_collabtools.contextPut("newsToolEnabled", Boolean.FALSE);
		}
		
		// update BigBlueButton form: only show when enabled
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_BIGBLUEBUTTON)) {
			addBigBlueButtonTool(ureq);
		} else {
			vc_collabtools.contextPut("bigBlueButtonToolEnabled", Boolean.FALSE);
		}
		
		// update Teams form: only show when enabled
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_TEAMS)) {
			addTeamsTool(ureq);
		} else {
			vc_collabtools.contextPut("teamsToolEnabled", Boolean.FALSE);
		}

		// update Zoom form: only show when enabled
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_ZOOM)) {
			addZoomTool(ureq);
		} else {
			removeZoomTool(ureq);
		}

		// update calendar form: only show when enabled
		boolean newCalendarEnabledState = collabTools.isToolEnabled(CollaborationTools.TOOL_CALENDAR);
		if (newCalendarEnabledState != lastCalendarEnabledState) {
			if (newCalendarEnabledState) {
				vc_collabtools.contextPut("calendarToolEnabled", Boolean.TRUE);
				int iCalendarAccess = CollaborationTools.CALENDAR_ACCESS_OWNERS;
				Long lCalendarAccess = collabTools.lookupCalendarAccess();
				if (lCalendarAccess != null) iCalendarAccess = lCalendarAccess.intValue();
				if (calendarForm != null) {
					this.removeAsListenerAndDispose(calendarForm);
				}
				calendarForm = new CalendarToolSettingsController(ureq, getWindowControl(), iCalendarAccess);
				calendarForm.setEnabled(!managed);
				listenTo(calendarForm);
				vc_collabtools.put("calendarform", calendarForm.getInitialComponent());

			} else {
				
				vc_collabtools.contextPut("calendarToolEnabled", Boolean.FALSE);

				// notify calendar components to refresh their calendars
				CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(
						new CalendarGUIModifiedEvent(), OresHelper.lookupType(CalendarManager.class)
				);
			}
			lastCalendarEnabledState = newCalendarEnabledState;
		}

		// update quota form: only show when enabled
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
			vc_collabtools.contextPut("folderToolEnabled", Boolean.TRUE);
			if(folderForm != null) {
				removeAsListenerAndDispose(folderForm);
			}
			Long lFolderAccess = collabTools.lookupFolderAccess();
			int access = lFolderAccess == null ? CollaborationTools.FOLDER_ACCESS_ALL : lFolderAccess.intValue();
			folderForm = new FolderToolSettingsController(ureq, getWindowControl(), access);
			folderForm.setEnabled(!managed);
			listenTo(folderForm);
			vc_collabtools.put("folderform", folderForm.getInitialComponent());
			if (ureq.getUserSession().getRoles().isAdministrator()) {
				vc_collabtools.put("quota", quotaCtr.getInitialComponent());
			}
		} else {
			vc_collabtools.contextPut("folderToolEnabled", Boolean.FALSE);
		}
		
	}


	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}

class ChoiceOfToolsForm extends FormBasicController {
	private CollaborationTools cts;
	private MultipleSelectionElement ms;
	
	private final String[] toolsKeys;
	private final String[] toolsValues;
	private final String[] cssClasses;
	private final String[] availableTools;
	
	@Autowired
	private InstantMessagingModule imModule;
	
	public ChoiceOfToolsForm(UserRequest ureq, WindowControl wControl, CollaborationTools cts, final String[] availableTools) {
		super(ureq, wControl);
		this.cts = cts;
		this.availableTools = availableTools;
		
		List<String> theKeys = new ArrayList<>();
		List<String> theValues = new ArrayList<>();
		List<String> theClasses = new ArrayList<>();
		for (int i=0; i<availableTools.length; i++) {
			String k = availableTools[i];
			if (k.equals(CollaborationTools.TOOL_CHAT) && (!imModule.isEnabled() || !imModule.isGroupEnabled())) {
				continue;
			}
			theKeys.add(""+i);
			theValues.add(translate("collabtools.named." + availableTools[i]));
			theClasses.add("o_sel_" + availableTools[i]);
		}
		
		toolsKeys = theKeys.toArray(new String[theKeys.size()]);
		toolsValues = theValues.toArray(new String[theValues.size()]);
		cssClasses = theClasses.toArray(new String[theClasses.size()]);
		
		initForm(ureq);
	}
	
	public void setEnabled(boolean enabled) {
		ms.setEnabled(enabled);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		ms = uifactory.addCheckboxesVertical("selection", "selection", formLayout, toolsKeys, toolsValues, cssClasses, null, 1);
		ms.setElementCssClass("o_sel_collab_tools");
		for (int i=0; i<availableTools.length; i++) {
			ms.select(""+i, cts.isToolEnabled(availableTools[i]));
		}
		ms.addActionListener(FormEvent.ONCLICK);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == ms && event.getCommand().equals("ONCLICK")) {
			fireEvent(ureq, new Event("ONCHANGE"));
		}
	}
	
	protected Collection<String> getSelected() {
		return ms.getSelectedKeys();
	}
}